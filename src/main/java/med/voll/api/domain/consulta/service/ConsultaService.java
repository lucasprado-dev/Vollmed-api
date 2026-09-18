package med.voll.api.domain.consulta.service;

import med.voll.api.domain.consulta.Consulta;
import med.voll.api.domain.consulta.dto.DadosAgendamentoConsultaDto;
import med.voll.api.domain.consulta.repository.ConsultaRepository;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.medico.repository.MedicoRepository;
import med.voll.api.domain.paciente.repository.PacienteRepository;
import med.voll.api.infra.exception.ValidacaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsultaService {

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    public void agendar(DadosAgendamentoConsultaDto dto){

        // Verificando se o id do paciente existe
        if (!pacienteRepository.existsById(dto.idPaciente())){
            // Se o paciente informado não existe no banco, interrompe o agendamento
            throw new ValidacaoException("Id do paciente informado, não existe!");
        }

        // Verificando se o id do médico existe (caso tenha sido informado)
        if (dto.idMedico() != null && !medicoRepository.existsById(dto.idMedico())){
            // Só valida a existência do médico se um id foi passado;
            // se o médico informado não existe, interrompe o agendamento
            throw new ValidacaoException("Id do medico informado, não existe!");
        }

        var paciente = pacienteRepository.getReferenceById(dto.idPaciente());
        var medico = escolherMedico(dto);
        var consulta = new Consulta(null, medico, paciente, dto.data());
        consultaRepository.save(consulta);
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

        // Escolhe aleatoriamente um médico livre naquela data e especialidade
        return medicoRepository.escolherMedicoAleatorioLivreNaData(dto.especialidade(), dto.data());
    }
}