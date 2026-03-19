package br.com.estapar.garagewebhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalTime;

public record SectorDTO(
        @JsonProperty("sector") String name,
        BigDecimal basePrice,
        Integer maxCapacity,
        LocalTime openHour,
        LocalTime closeHour,
        Integer durationLimitMinutes
) {
}
