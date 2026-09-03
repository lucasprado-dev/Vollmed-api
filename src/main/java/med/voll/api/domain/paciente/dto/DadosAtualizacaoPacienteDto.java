package med.voll.api.domain.paciente.dto;

import jakarta.validation.constraints.NotNull;
import med.voll.api.domain.endereco.dto.DadosEnderecoDto;

public record DadosAtualizacaoPacienteDto(
        @NotNull
        Long id,
        String nome,
        String telefone,
        DadosEnderecoDto endereco
) {
}
