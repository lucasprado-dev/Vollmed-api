package med.voll.api.domain.consulta.validacoes;

import med.voll.api.domain.consulta.dto.DadosAgendamentoConsultaDto;
import med.voll.api.infra.exception.ValidacaoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorHorarioFuncionamentoClinicaTest {

    private final ValidadorHorarioFuncionamentoClinica validador = new ValidadorHorarioFuncionamentoClinica();

    @Test
    @DisplayName("Deveria lançar exceção quando o dia da semana é domingo e o horário é às 10:00")
    void cenario1() {
        var dto = new DadosAgendamentoConsultaDto(1L, 2L, LocalDateTime.of(2026, 9, 27, 10, 00), null);
        assertThrows(ValidacaoException.class, () -> validador.validar(dto));
        /*
        assertThrows(                        // "eu garanto que vai dar erro..."
        ValidacaoException.class,        // "...e o erro vai ser deste tipo..."
        () -> validador.validar(dto)   // "...quando executar isto"
         */
    }

    @Test
    @DisplayName("Deveria lançar exceção quando o dia da semana é segunda e o horário é às 06:59")
    void cenario2() {
        var dto = new DadosAgendamentoConsultaDto(1L, 2L, LocalDateTime.of(2026, 9, 21, 06, 59), null);
        assertThrows(ValidacaoException.class, () -> validador.validar(dto));
    }

    @Test
    @DisplayName("Não deveria lançar exceção quando o dia da semana é segunda e o horário é às 07:00")
    void cenario3() {
        var dto = new DadosAgendamentoConsultaDto(1L, 2L, LocalDateTime.of(2026, 9, 21, 07, 00), null);
        assertDoesNotThrow(() -> validador.validar(dto));
    }

    @Test
    @DisplayName("Não deveria lançar exceção quando o dia da semana é segunda e o horário é às 10:00")
    void cenario4() {
        var dto = new DadosAgendamentoConsultaDto(1L, 2L, LocalDateTime.of(2026, 9, 21, 10, 00), null);
        assertDoesNotThrow(() -> validador.validar(dto));
    }

    @Test
    @DisplayName("Não deveria lançar exceção quando o dia da semana é segunda e o horário é às 18:00")
    void cenario5() {
        var dto = new DadosAgendamentoConsultaDto(1L, 2L, LocalDateTime.of(2026, 9, 21, 18, 00), null);
        assertDoesNotThrow(() -> validador.validar(dto));
    }

    @Test
    @DisplayName("Não deveria lançar exceção quando o dia da semana é segunda e o horário é às 18:59")
    void cenario6() {
        var dto = new DadosAgendamentoConsultaDto(1L, 2L, LocalDateTime.of(2026, 9, 21, 18, 59), null);
        assertDoesNotThrow(() -> validador.validar(dto));
    }

    @Test
    @DisplayName("Deveria lançar exceção quando o dia da semana é segunda e o horário é às 19:00")
    void cenario7() {
        var dto = new DadosAgendamentoConsultaDto(1L, 2L, LocalDateTime.of(2026, 9, 21, 19, 00), null);
        assertThrows(ValidacaoException.class, () -> validador.validar(dto));
    }

    @Test
    @DisplayName("Não deveria lançar exceção quando o dia da semana é sabado e o horário é às 10:00")
    void cenario8() {
        var dto = new DadosAgendamentoConsultaDto(1L, 2L, LocalDateTime.of(2026, 9, 26, 10, 00), null);
        assertDoesNotThrow(() -> validador.validar(dto));
    }
}