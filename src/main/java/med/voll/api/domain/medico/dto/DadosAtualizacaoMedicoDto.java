package med.voll.api.domain.medico.dto;

import jakarta.validation.constraints.NotNull;
import med.voll.api.domain.endereco.dto.DadosEnderecoDto;

public record DadosAtualizacaoMedicoDto(
        @NotNull
        Long id,
        String nome,
        String telefone,
        DadosEnderecoDto endereco) {
}
