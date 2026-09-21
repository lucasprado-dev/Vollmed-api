package med.voll.api.domain.consulta.validacoes;

import med.voll.api.domain.consulta.dto.DadosAgendamentoConsultaDto;
import med.voll.api.domain.paciente.repository.PacienteRepository;
import med.voll.api.infra.exception.ValidacaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorPacienteAtivo implements ValidadorAgendamentoDeConsulta {

    @Autowired
    private PacienteRepository pacienteRepository;

    public void validar(DadosAgendamentoConsultaDto dto){

        var pacienteEstaAtivo = pacienteRepository.findAtivoById(dto.idPaciente());
        if (!pacienteEstaAtivo) {
            throw new ValidacaoException("Consulta não pode ser agendada com paciente excluído");
        }
    }
}
