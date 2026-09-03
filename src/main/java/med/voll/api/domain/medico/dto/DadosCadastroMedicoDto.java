package med.voll.api.domain.medico.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import med.voll.api.domain.endereco.dto.DadosEnderecoDto;
import med.voll.api.domain.medico.Especialidade;

public record DadosCadastroMedicoDto(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @NotBlank(message = "Telefone é obrigatório")
        String telefone,

        @NotBlank(message = "CRM é obrigatório")
        @Pattern(regexp = "\\d{4,6}", message = "O CRM deve conter entre 4 e 6 dígitos numéricos")
        String crm,

        @NotNull(message = "Especialidade é obrigatório")
        Especialidade especialidade,

        @NotNull(message = "Endereço é obrigatório")
        @Valid
        DadosEnderecoDto endereco

) {}