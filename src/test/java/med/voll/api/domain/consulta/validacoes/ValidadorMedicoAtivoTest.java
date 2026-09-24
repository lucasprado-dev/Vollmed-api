package med.voll.api.domain.consulta.validacoes;

import med.voll.api.domain.consulta.dto.DadosAgendamentoConsultaDto;
import med.voll.api.domain.medico.repository.MedicoRepository;
import med.voll.api.infra.exception.ValidacaoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidadorMedicoAtivoTest {

    @Mock
    // "Me dá um repository de mentira, sem banco."
    private MedicoRepository repository;

    @InjectMocks
    // "Cria o validador de verdade e entrega esse repository de mentira pra ele."
    private ValidadorMedicoAtivo validador;

    @Test
    @DisplayName("Deveria lançar exceção quando o médico estiver inativo")
    void medicoInativo() {
        // ARRANGE
        // idMedico, idPaciente, data, especialidade

        // "Monta os dados de uma consulta com o médico 1."
        var dto = new DadosAgendamentoConsultaDto(1L, 5L, LocalDateTime.now().plusDays(1), null);

        // TODO 1: ensine o mock a responder false para findAtivoById(1L)
        when(repository.findAtivoById(1L)).thenReturn(false);

        // ACT + ASSERT
        // TODO 2: assertThrows(ValidacaoException.class, () -> ...);
        assertThrows(ValidacaoException.class, () -> validador.validar(dto));
    }

    @Test
    @DisplayName("Não deveria lançar exceção quando o médico estiver ativo")
    void medicoAtivo() {
        var dto = new DadosAgendamentoConsultaDto(1L, 5L, LocalDateTime.now().plusDays(1), null);
        when(repository.findAtivoById(1L)).thenReturn(true);
        assertDoesNotThrow(() -> validador.validar(dto));
    }

    @Test
    @DisplayName("Não deveria nem consultar o banco quando o médico não for informado")
    void medicoNaoInformado() {
        // TODO 4: monte um DTO com idMedico = null,
        //         chame o validar e depois use verifyNoInteractions(repository)
        //         (isso confere que o mock NUNCA foi chamado)
        var dto = new DadosAgendamentoConsultaDto(null, 5L, LocalDateTime.now().plusDays(1), null);
        validador.validar(dto);
        verifyNoInteractions(repository);
    }
}
