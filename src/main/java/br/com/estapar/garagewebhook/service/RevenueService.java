package br.com.estapar.garagewebhook.service;

import br.com.estapar.garagewebhook.domain.model.ParkingSession;
import br.com.estapar.garagewebhook.dto.RevenueRequestDTO;
import br.com.estapar.garagewebhook.dto.RevenueResponseDTO;
import br.com.estapar.garagewebhook.repository.ParkingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class RevenueService {

    private final ParkingSessionRepository sessionRepository;

    public RevenueResponseDTO calculateRevenue(RevenueRequestDTO data){
        LocalDate date = data.date();

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        BigDecimal amount = sessionRepository.sumRevenueByPeriod(start, end, data.sector().toUpperCase());

        Instant timestamp = date.atStartOfDay().toInstant(ZoneOffset.UTC);

        return new RevenueResponseDTO(amount,"BRL",timestamp);
    }
}
