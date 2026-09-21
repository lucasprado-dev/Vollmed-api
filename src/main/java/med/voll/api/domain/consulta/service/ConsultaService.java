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

@Service
public class ConsultaService {

    private static final Logger log = LoggerFactory.getLogger(ConsultaService.class);

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private List<ValidadorAgendamentoDeConsulta> validacoes;

    public DadosDetalhamentoConsultaDto agendar(DadosAgendamentoConsultaDto dto){

        log.info("Iniciando agendamento: idPaciente={}, idMedico={}, especialidade={}, data={}",
                dto.idPaciente(), dto.idMedico(), dto.especialidade(), dto.data());

        // Verificando se o id do paciente existe
        if (!pacienteRepository.existsById(dto.idPaciente())){
            // Se o paciente informado não existe no banco, interrompe o agendamento
            log.warn("Agendamento recusado: paciente {} não existe", dto.idPaciente());
            throw new ValidacaoException("Id do paciente informado, não existe!");
        }

        // Verificando se o id do médico existe (caso tenha sido informado)
        if (dto.idMedico() != null && !medicoRepository.existsById(dto.idMedico())){
            // Só valida a existência do médico se um id foi passado;
            // se o médico informado não existe, interrompe o agendamento
            log.warn("Agendamento recusado: médico {} não existe", dto.idMedico());
            throw new ValidacaoException("Id do medico informado, não existe!");
        }

        //Validações
        validacoes.forEach(v -> v.validar(dto));
        log.debug("Validações concluídas para paciente {}", dto.idPaciente());

        var paciente = pacienteRepository.getReferenceById(dto.idPaciente());
        var medico = escolherMedico(dto);

        if (medico == null) {
            log.warn("Sem médico disponível: especialidade={}, data={}", dto.especialidade(), dto.data());
            throw new ValidacaoException("Não existe médico disponivel nesta data");
        }

        var consulta = new Consulta(null, medico, paciente, dto.data());
        consultaRepository.save(consulta);

        log.info("Consulta agendada: id={}, medico={}, paciente={}",
                consulta.getId(), medico.getId(), paciente.getId());

        return new DadosDetalhamentoConsultaDto(consulta);
    }

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
        return medicoRepository.escolherMedicoAleatorioLivreNaData(dto.especialidade(), dto.data());
    }
}