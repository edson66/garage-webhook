package br.com.estapar.garagewebhook.dto;

import java.math.BigDecimal;

public record SpotDTO(
        Long id,
        String sector,
        BigDecimal lat,
        BigDecimal lng,
        Boolean occupied
) {
}
