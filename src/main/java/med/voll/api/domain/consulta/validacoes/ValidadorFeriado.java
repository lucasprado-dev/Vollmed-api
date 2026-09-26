package med.voll.api.domain.consulta.validacoes;

import med.voll.api.domain.consulta.dto.DadosAgendamentoConsultaDto;
import med.voll.api.domain.medico.repository.MedicoRepository;
import med.voll.api.infra.exception.ValidacaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorFeriado implements ValidadorAgendamentoDeConsulta {

    @Autowired
    private MedicoRepository medicoRepository;

    public void validar(DadosAgendamentoConsultaDto dto){
        //escolha do medico opcional
        if(dto.idMedico() == null){
            return;
        }

        var medicoEstaAtivo = medicoRepository.findAtivoById(dto.idMedico());
        if (!medicoEstaAtivo) {
            throw new ValidacaoException("Consulta não pode ser agendada com médico excluído");
        }
    }
}
