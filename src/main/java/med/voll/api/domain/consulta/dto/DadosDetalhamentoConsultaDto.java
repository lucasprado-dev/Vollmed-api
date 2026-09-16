package med.voll.api.domain.consulta.dto;

import java.time.LocalDateTime;

public record DadosDetalhamentoConsultaDto(
        Long id,
        Long idMedico,
        Long idPaciente,
        LocalDateTime data
) {}
