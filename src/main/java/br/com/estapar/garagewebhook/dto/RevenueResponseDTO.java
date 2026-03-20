package br.com.estapar.garagewebhook.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record RevenueResponseDTO(
        BigDecimal amount,
        String currency,
        Instant timestamp
) {
}
