package br.com.estapar.garagewebhook.dto;

import br.com.estapar.garagewebhook.domain.enums.EventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WebhookEventDTO(
        String licensePlate,
        LocalDateTime entryTime,
        LocalDateTime exitTime,
        BigDecimal lat,
        BigDecimal lng,
        EventType eventType
) {
}
