package br.com.estapar.garagewebhook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RevenueRequestDTO(
        @NotNull
        LocalDate date,
        @NotBlank
        String sector
) {
}
