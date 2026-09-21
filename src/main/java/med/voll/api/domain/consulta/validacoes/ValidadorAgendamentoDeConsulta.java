package med.voll.api.domain.consulta.validacoes;

import med.voll.api.domain.consulta.dto.DadosAgendamentoConsultaDto;

public interface ValidadorAgendamentoDeConsulta {

    void validar(DadosAgendamentoConsultaDto dto);
}
