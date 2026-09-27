package med.voll.api.domain.consulta.validacoes;

import med.voll.api.domain.consulta.dto.DadosAgendamentoConsultaDto;
import med.voll.api.infra.exception.ValidacaoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidadorFeriadoTest {

    @Mock
    private CalendarioFeriados feriados;

    @InjectMocks
    private ValidadorFeriado validador;

    @Test
    @DisplayName("Deveria lançar exceção quando a data da consulta for feriado")
    void feriado() {
        // ARRANGE
        var dto = new DadosAgendamentoConsultaDto(1L, 2L,
                LocalDateTime.of(2026, 12, 25, 12, 00), null);

        when(feriados.ehFeriado(LocalDate.of(2026, 12, 25))).thenReturn(true);

        // ACT + ASSERT
        assertThrows(ValidacaoException.class, () -> validador.validar(dto));
    }

    @Test
    @DisplayName("Não deveria lançar exceção quando a data da consulta não for feriado")
    void diaNormal() {
        var dto = new DadosAgendamentoConsultaDto(1L, 2L,
                LocalDateTime.of(2026, 12, 24, 12, 00), null);

        when(feriados.ehFeriado(LocalDate.of(2026, 12, 24))).thenReturn(false);

        assertDoesNotThrow(() -> validador.validar(dto));
    }

    @Test
    @DisplayName("Deveria perguntar ao calendário só pelo dia, sem o horário")
    void perguntaSoPeloDia() {
        var dto = new DadosAgendamentoConsultaDto(1L, 2L,
                LocalDateTime.of(2026, 9, 28, 15, 30), null);

        validador.validar(dto);

        verify(feriados).ehFeriado(LocalDate.of(2026, 9, 28));
    }
}
