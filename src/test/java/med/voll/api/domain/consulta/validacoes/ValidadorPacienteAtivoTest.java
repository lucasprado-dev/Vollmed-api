package med.voll.api.domain.consulta.validacoes;

import med.voll.api.domain.consulta.dto.DadosAgendamentoConsultaDto;
import med.voll.api.domain.paciente.repository.PacienteRepository;
import med.voll.api.infra.exception.ValidacaoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidadorPacienteAtivoTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private ValidadorPacienteAtivo validador;

    @Test
    @DisplayName("Deveria lançar exceção quando o paciente estiver inativo")
    void pacienteInativo(){
        var dto = new DadosAgendamentoConsultaDto(1L, 2L, LocalDateTime.now().plusDays(1), null);
        when(pacienteRepository.findAtivoById(2L)).thenReturn(false);
        assertThrows(ValidacaoException.class, () -> validador.validar(dto));
        /*
        assertThrows(                        // "eu garanto que vai dar erro..."
        ValidacaoException.class,        // "...e o erro vai ser deste tipo..."
        () -> validador.validar(dto)   // "...quando executar isto"
         */
    }

    @Test
    @DisplayName("Não deveria lançar exceção quando o paciente estiver ativo")
    void pacienteAtivo(){
        var dto = new DadosAgendamentoConsultaDto(1L, 2L, LocalDateTime.now().plusDays(1), null);
        when(pacienteRepository.findAtivoById(2L)).thenReturn(true);
        assertDoesNotThrow(() -> validador.validar(dto));
        /*
        assertThrows(                        // "eu garanto que não vai dar erro..."
        ValidacaoException.class,        // "...e o erro vai ser deste tipo..."
        () -> validador.validar(dto)   // "...quando executar isto"
         */
    }

}