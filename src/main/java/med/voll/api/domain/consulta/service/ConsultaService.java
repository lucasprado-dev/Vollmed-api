package med.voll.api.domain.consulta.service;

import med.voll.api.domain.consulta.Consulta;
import med.voll.api.domain.consulta.dto.DadosAgendamentoConsultaDto;
import med.voll.api.domain.consulta.dto.DadosDetalhamentoConsultaDto;
import med.voll.api.domain.consulta.repository.ConsultaRepository;
import med.voll.api.domain.consulta.validacoes.ValidadorAgendamentoDeConsulta;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.medico.repository.MedicoRepository;
import med.voll.api.domain.paciente.repository.PacienteRepository;
import med.voll.api.infra.exception.ValidacaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

// @Service: registra a classe como bean do Spring (camada de serviço).
// É aqui que ficam as REGRAS DE NEGÓCIO do agendamento. O controller só recebe a requisição
// e chama este serviço; quem decide se a consulta pode ou não ser marcada é ele.
// Analogia: é a "recepcionista da clínica". O paciente pede o horário, ela confere tudo,
// escolhe o médico e só então registra a consulta na agenda.
@Service
public class ConsultaService {

    // Logger do SLF4J (a interface de log padrão do Spring Boot).
    // static final: um único logger para a classe toda, criado uma vez.
    // Níveis usados aqui: info (fluxo normal), warn (recusa esperada, ex: dado inválido),
    // debug (detalhe para investigação, normalmente desligado em produção).
    private static final Logger log = LoggerFactory.getLogger(ConsultaService.class);

    // Repositories: acesso às tabelas de consultas, médicos e pacientes.
    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    // Lista com TODAS as classes que implementam ValidadorAgendamentoDeConsulta.
    // O Spring encontra sozinho cada @Component que implementa essa interface e injeta todas aqui.
    // Isso é o padrão Strategy: cada regra (horário de funcionamento, antecedência mínima,
    // paciente ativo, etc.) fica em uma classe separada.
    // Vantagem: para criar uma regra nova, você só cria uma classe nova. Este serviço não muda.
    // Analogia: é o "checklist" da recepcionista, onde cada item foi escrito por uma pessoa diferente.
    @Autowired
    private List<ValidadorAgendamentoDeConsulta> validacoes;

    // Metodo principal: recebe os dados do agendamento e devolve o detalhamento da consulta criada.
    // Se qualquer regra falhar, lança ValidacaoException, que o TratadorDeErros converte em 400.
    public DadosDetalhamentoConsultaDto agendar(DadosAgendamentoConsultaDto dto){

        // {} é o placeholder do SLF4J: os valores entram na ordem, sem concatenar String na mão.
        log.info("Iniciando agendamento: idPaciente={}, idMedico={}, especialidade={}, data={}",
                dto.idPaciente(), dto.idMedico(), dto.especialidade(), dto.data());

        // Verificando se o id do paciente existe
        // existsById gera um SELECT leve (só confirma se o registro existe, não traz os dados).
        if (!pacienteRepository.existsById(dto.idPaciente())){
            // Se o paciente informado não existe no banco, interrompe o agendamento
            log.warn("Agendamento recusado: paciente {} não existe", dto.idPaciente());
            throw new ValidacaoException("Id do paciente informado, não existe!");
        }

        // Verificando se o id do médico existe (caso tenha sido informado)
        // O idMedico é opcional: se vier null, o sistema sorteia um médico depois.
        // O "!= null" vem primeiro de propósito: se for null, o && já para e nem consulta o banco.
        if (dto.idMedico() != null && !medicoRepository.existsById(dto.idMedico())){
            // Só valida a existência do médico se um id foi passado;
            // se o médico informado não existe, interrompe o agendamento
            log.warn("Agendamento recusado: médico {} não existe", dto.idMedico());
            throw new ValidacaoException("Id do medico informado, não existe!");
        }

        //Validações
        // Percorre a lista de validadores e executa cada um com os dados do agendamento.
        // Se algum lançar ValidacaoException, o metodo para aqui e nada é salvo.
        validacoes.forEach(v -> v.validar(dto));
        log.debug("Validações concluídas para paciente {}", dto.idPaciente());

        // getReferenceById: devolve uma "referência preguiçosa" (proxy) ao paciente, sem fazer SELECT agora.
        // Serve bem aqui porque só precisamos do paciente para ligar a consulta a ele (chave estrangeira).
        // A existência já foi confirmada lá em cima, então é seguro.
        var paciente = pacienteRepository.getReferenceById(dto.idPaciente());

        // Define qual médico vai atender: o que foi informado ou um sorteado (ver metodo abaixo).
        var medico = escolherMedico(dto);

        // Só chega null quando o médico não foi informado E o sorteio não achou ninguém livre.
        if (medico == null) {
            log.warn("Sem médico disponível: especialidade={}, data={}", dto.especialidade(), dto.data());
            throw new ValidacaoException("Não existe médico disponivel nesta data");
        }

        // Monta a entidade Consulta. O primeiro parâmetro (id) é null porque quem gera é o banco (auto increment).
        var consulta = new Consulta(null, medico, paciente, dto.data());

        // save: executa o INSERT. Depois disso, o objeto "consulta" já tem o id preenchido.
        consultaRepository.save(consulta);

        log.info("Consulta agendada: id={}, medico={}, paciente={}",
                consulta.getId(), medico.getId(), paciente.getId());

        // Converte a entidade em DTO de resposta (nunca devolva a entidade direto no endpoint).
        return new DadosDetalhamentoConsultaDto(consulta);
    }

    // private: só é usado dentro desta classe. Isola a lógica de "quem atende a consulta".
    private Medico escolherMedico(DadosAgendamentoConsultaDto dto) {
        // Se o id do médico foi informado, usa ele diretamente (sem sorteio)
        if (dto.idMedico() != null){
            return medicoRepository.getReferenceById(dto.idMedico());
        }
        // Se não veio médico, a especialidade é obrigatória para poder sortear um médico livre
        if (dto.especialidade() == null){
            throw new ValidacaoException("Especialidade é obrigatoria quando o médico não for passado");
        }

        log.debug("Médico não informado, sorteando por especialidade={} na data={}",
                dto.especialidade(), dto.data());

        // Escolhe aleatoriamente um médico livre naquela data e especialidade
        // É uma query customizada no MedicoRepository (provavelmente @Query com SQL nativo/JPQL).
        // Retorna null se nenhum médico da especialidade estiver livre naquele horário.
        return medicoRepository.escolherMedicoAleatorioLivreNaData(dto.especialidade(), dto.data());
    }
}