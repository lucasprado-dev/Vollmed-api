package med.voll.api.domain.consulta.validacoes;

import java.time.LocalDate;

public interface CalendarioFeriados {

    boolean ehFeriado(LocalDate data);
}
