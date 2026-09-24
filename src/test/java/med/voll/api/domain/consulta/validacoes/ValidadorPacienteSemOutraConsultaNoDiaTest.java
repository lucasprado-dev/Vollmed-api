package med.voll.api.domain.consulta.validacoes;

import med.voll.api.domain.consulta.dto.DadosAgendamentoConsultaDto;
import med.voll.api.domain.consulta.repository.ConsultaRepository;
import med.voll.api.infra.exception.ValidacaoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidadorPacienteSemOutraConsultaNoDiaTest {

    @Mock
    private ConsultaRepository repository;

    @InjectMocks
    private ValidadorPacienteSemOutraConsultaNoDia validador;

    @Test
    @DisplayName("Deveria consultar o dia inteiro de funcionamento, das 07:00 às 18:00")
    void pacienteSemConsulta() {
        var dto = new DadosAgendamentoConsultaDto(1L, 2L,
                LocalDateTime.of(2026, 9, 28, 10, 30), null);

        // O validador.validar(dto) é o código de produção fazendo o trabalho dele: pergunta ao banco se o paciente já tem consulta e lança exceção se tiver. Ele verifica o paciente.
        validador.validar(dto);

        // O verify(...) é o teste conferindo se o validador fez o trabalho direito: "você perguntou ao banco o intervalo certo?". Ele verifica o validador.
        verify(repository).existsByPacienteIdAndDataBetween(
                2L,
                LocalDateTime.of(2026, 9, 28, 7, 0),
                LocalDateTime.of(2026, 9, 28, 18, 0)
        );
    }

    @Test
    @DisplayName("Deveria lançar a exceção quando o paciente tiver uma consulta")
    void pacienteComConsulta() {
        var dto = new DadosAgendamentoConsultaDto(1L, 2L,
                LocalDateTime.of(2026, 9, 28, 10, 30), null);

        validador.validar(dto);

        when(repository.existsByPacienteIdAndDataBetween(
                2L,
                LocalDateTime.of(2026, 9, 28, 7, 0),
                LocalDateTime.of(2026, 9, 28, 18, 0)
        )).thenReturn(true);

        assertThrows(ValidacaoException.class, () -> validador.validar(dto));
    }

}