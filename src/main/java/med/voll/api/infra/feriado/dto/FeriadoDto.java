package med.voll.api.infra.feriado.dto;

import java.time.LocalDate;

public record FeriadoDto(LocalDate date, String name) {}