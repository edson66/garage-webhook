package br.com.estapar.garagewebhook.service;

import br.com.estapar.garagewebhook.dto.RevenueRequestDTO;
import br.com.estapar.garagewebhook.dto.RevenueResponseDTO;
import br.com.estapar.garagewebhook.repository.ParkingSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevenueServiceTest {

    @Mock
    ParkingSessionRepository sessionRepository;

    @InjectMocks
    RevenueService service;

    @Test
    @DisplayName("Should calculate revenue with correct date boundaries and exact amount")
    void calculateRevenueCase1() {
        LocalDate targetDate = LocalDate.of(2026, 3, 20);
        var requestDto = new RevenueRequestDTO(targetDate, "A");

        LocalDateTime expectedStart = targetDate.atStartOfDay();
        LocalDateTime expectedEnd = targetDate.plusDays(1).atStartOfDay();
        BigDecimal expectedAmount = new BigDecimal("150.50");

        when(sessionRepository.sumRevenueByPeriod(expectedStart, expectedEnd, "A"))
                .thenReturn(expectedAmount);

        RevenueResponseDTO response = service.calculateRevenue(requestDto);

        assertNotNull(response);
        assertEquals(expectedAmount, response.amount());
        assertEquals("BRL", response.currency());
        assertEquals(targetDate.atStartOfDay().toInstant(ZoneOffset.UTC), response.timestamp());
    }

    @Test
    @DisplayName("Should convert sector to uppercase before querying the database")
    void calculateRevenueCase2() {
        LocalDate targetDate = LocalDate.of(2026, 3, 20);
        var requestDto = new RevenueRequestDTO(targetDate, "b");

        LocalDateTime expectedStart = targetDate.atStartOfDay();
        LocalDateTime expectedEnd = targetDate.plusDays(1).atStartOfDay();
        BigDecimal expectedAmount = BigDecimal.ZERO;

        when(sessionRepository.sumRevenueByPeriod(expectedStart, expectedEnd, "B"))
                .thenReturn(expectedAmount);

        RevenueResponseDTO response = service.calculateRevenue(requestDto);

        assertEquals(expectedAmount, response.amount());
        verify(sessionRepository).sumRevenueByPeriod(expectedStart, expectedEnd, "B");
    }
}