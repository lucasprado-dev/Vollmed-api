package med.voll.api.domain.consulta.validacoes;

import med.voll.api.domain.consulta.dto.DadosAgendamentoConsultaDto;
import med.voll.api.infra.exception.ValidacaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorFeriado implements ValidadorAgendamentoDeConsulta {

    @Autowired
    private CalendarioFeriados feriados;

    public void validar(DadosAgendamentoConsultaDto dto) {
        var dataConsulta = dto.data();
        if (feriados.ehFeriado(dataConsulta.toLocalDate())) {
            throw new ValidacaoException("Consulta não pode ser agendada em feriado");
        }
    }
}
