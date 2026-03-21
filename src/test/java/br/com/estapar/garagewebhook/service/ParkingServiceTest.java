package br.com.estapar.garagewebhook.service;

import br.com.estapar.garagewebhook.domain.enums.EventType;
import br.com.estapar.garagewebhook.domain.exception.GarageFullException;
import br.com.estapar.garagewebhook.domain.exception.NotParkedException;
import br.com.estapar.garagewebhook.domain.model.ParkingSession;
import br.com.estapar.garagewebhook.domain.model.Sector;
import br.com.estapar.garagewebhook.domain.model.Spot;
import br.com.estapar.garagewebhook.dto.WebhookEventDTO;
import br.com.estapar.garagewebhook.repository.ParkingSessionRepository;
import br.com.estapar.garagewebhook.repository.SpotRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @Mock
    private SpotRepository spotRepository;

    @Mock
    private ParkingSessionRepository sessionRepository;

    @InjectMocks
    private ParkingService parkingService;

    @Captor
    ArgumentCaptor<ParkingSession> sessionCaptor;

    @Test
    @DisplayName("Must complete the entry successfully")
    void processEntryCase1() {

        var dto = new WebhookEventDTO("TEST", LocalDateTime.now().minusDays(2),
                null, null,null,EventType.ENTRY);

        when(spotRepository.count()).thenReturn(30L);
        when(spotRepository.countByOccupiedTrue()).thenReturn(16L);

        parkingService.processEntry(dto);

        verify(sessionRepository).save(sessionCaptor.capture());

        ParkingSession savedSession = sessionCaptor.getValue();

        assertEquals("TEST", savedSession.getLicensePlate());
        assertNotNull(savedSession.getEntryTime());
        assertNotNull(savedSession.getPriceModifierPercentage());
        assertEquals(new BigDecimal("10.00"),savedSession.getPriceModifierPercentage());
    }

    @Test
    @DisplayName("Should throw exception when garage is full")
    void processEntryCase2() {

        var dto = new WebhookEventDTO("TEST", LocalDateTime.now().minusDays(2),
                null, null,null,EventType.ENTRY);

        when(spotRepository.count()).thenReturn(30L);
        when(spotRepository.countByOccupiedTrue()).thenReturn(30L);

        assertThrows(GarageFullException.class,() -> parkingService.processEntry(dto));
        verify(sessionRepository,never()).save(any(ParkingSession.class));
    }

    @Test
    @DisplayName("Must complete the park successfully")
    void processParkedCase1() {

        var dto = new WebhookEventDTO("TEST", null,
                null, BigDecimal.valueOf(12.12),BigDecimal.valueOf(12.25),
                EventType.PARKED);

        var sectorMock = new Sector();
        sectorMock.setName("B");
        sectorMock.setOpenHour(LocalTime.of(8,0));
        sectorMock.setCloseHour(LocalTime.of(18,0));

        var spotMock = new Spot();
        spotMock.setSector(sectorMock);

        var sessionMock = new ParkingSession();
        sessionMock.setEntryTime(LocalDateTime.of(2026,2,2,18,0));

        when(spotRepository.findByLatAndLng(dto.lat(),dto.lng()))
                .thenReturn(Optional.of(spotMock));
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(dto.licensePlate()))
                .thenReturn(Optional.of(sessionMock));

        parkingService.processParked(dto);

        verify(spotRepository).save(spotMock);
        verify(sessionRepository).save(sessionMock);
        assertTrue(spotMock.getOccupied());
        assertEquals(spotMock,sessionMock.getSpot());

    }

    @Test
    @DisplayName("Should throw exception when spot entity is not found")
    void processParkedCase2() {

        var dto = new WebhookEventDTO("TEST", null,
                null, BigDecimal.valueOf(12.12),BigDecimal.valueOf(12.25),
                EventType.PARKED);

        var ex = assertThrows(EntityNotFoundException.class,
                () -> parkingService.processParked(dto));

        assertTrue(ex.getMessage().contains("Vaga não encontrada para lat/lng: " + dto.lat() +
                ", " + dto.lng()));
    }

    @Test
    @DisplayName("Should throw exception when session entity is not found")
    void processParkedCase3() {

        var dto = new WebhookEventDTO("TEST", null,
                null, BigDecimal.valueOf(12.12),BigDecimal.valueOf(12.25),
                EventType.PARKED);

        var spotMock = new Spot();

        when(spotRepository.findByLatAndLng(dto.lat(),dto.lng()))
                .thenReturn(Optional.of(spotMock));

        var ex = assertThrows(EntityNotFoundException.class,
                () -> parkingService.processParked(dto));

        assertTrue(ex.getMessage().contains("Carro não encontrado ou já saiu: " + dto.licensePlate()));

    }

    @Test
    @DisplayName("Should override occupied spot")
    void processParkedCase4() {

        var dto = new WebhookEventDTO("TEST", null,
                null, new BigDecimal("1.20"),new BigDecimal("1.40"),
                EventType.PARKED);

        var sectorMock = new Sector();
        sectorMock.setName("B");
        sectorMock.setOpenHour(LocalTime.of(8,0));
        sectorMock.setCloseHour(LocalTime.of(18,0));

        var spotMock = new Spot();
        spotMock.setOccupied(true);
        spotMock.setSector(sectorMock);

        var sessionMock = new ParkingSession();
        sessionMock.setEntryTime(LocalDateTime.of(2026,2,2,17,0));

        when(spotRepository.findByLatAndLng(dto.lat(), dto.lng()))
                .thenReturn(Optional.of(spotMock));

        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(dto.licensePlate()))
                .thenReturn(Optional.of(sessionMock));

        parkingService.processParked(dto);

        assertTrue(spotMock.getOccupied());
        assertEquals(spotMock, sessionMock.getSpot());

    }

    @Test
    @DisplayName("Must complete the exit successfully")
    void processExitCase1() {

        var dto = new WebhookEventDTO("TEST", null,
                LocalDateTime.of(2026,2,2,18,0), null,null,
                EventType.EXIT);

        var sectorMock = new Sector();
        sectorMock.setName("B");
        sectorMock.setOpenHour(LocalTime.of(8,0));
        sectorMock.setCloseHour(LocalTime.of(18,0));
        sectorMock.setDurationLimitMinutes(60);
        sectorMock.setBasePrice(new BigDecimal("4.10"));

        var spotMock = new Spot();
        spotMock.setSector(sectorMock);

        var sessionMock = new ParkingSession();
        sessionMock.setEntryTime(LocalDateTime.of(2026,2,2,17,0));
        sessionMock.setSpot(spotMock);
        sessionMock.setPriceModifierPercentage(BigDecimal.ZERO);

        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(dto.licensePlate()))
                .thenReturn(Optional.of(sessionMock));

        parkingService.processExit(dto);

        assertEquals(new BigDecimal("4.10"),sessionMock.getTotalPaid());
        assertFalse(spotMock.getOccupied());
        verify(spotRepository).save(spotMock);
        verify(sessionRepository).save(sessionMock);
        assertNotNull(sessionMock.getExitTime());
    }

    @Test
    @DisplayName("Should throw exception when vehicle is not parked")
    void processExitCase2() {

        var dto = new WebhookEventDTO("TEST", null,
                LocalDateTime.of(2026,2,2,18,0), null,null,
                EventType.EXIT);

        var sessionMock = new ParkingSession();
        sessionMock.setEntryTime(LocalDateTime.of(2026,2,2,17,0));

        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(dto.licensePlate()))
                .thenReturn(Optional.of(sessionMock));

        var ex = assertThrows(NotParkedException.class,
                () -> parkingService.processExit(dto));

        assertTrue(ex.getMessage().contains("Veículo ainda não estacionado!"));
    }

    @Test
    @DisplayName("Should throw exception when session entity is not found")
    void processExitCase3() {

        var dto = new WebhookEventDTO("TEST", null,
                LocalDateTime.of(2026,2,2,18,0), null,null,
                EventType.EXIT);


        var ex = assertThrows(EntityNotFoundException.class,
                () -> parkingService.processExit(dto));

        assertTrue(ex.getMessage().contains("Carro não encontrado ou já saiu: " + dto.licensePlate()));
    }

    @Test
    @DisplayName("Should charge two hours when more than one hour,and multiply with modifier")
    void processExitCase4() {

        var dto = new WebhookEventDTO("TEST", null,
                LocalDateTime.of(2026,2,2,17,30), null,null,
                EventType.EXIT);

        var sectorMock = new Sector();
        sectorMock.setName("B");
        sectorMock.setOpenHour(LocalTime.of(8,0));
        sectorMock.setCloseHour(LocalTime.of(18,0));
        sectorMock.setDurationLimitMinutes(60);
        sectorMock.setBasePrice(new BigDecimal("4.10"));

        var spotMock = new Spot();
        spotMock.setSector(sectorMock);

        var sessionMock = new ParkingSession();
        sessionMock.setEntryTime(LocalDateTime.of(2026,2,2,16,0));
        sessionMock.setSpot(spotMock);
        sessionMock.setPriceModifierPercentage(new BigDecimal("25.00"));

        BigDecimal decimalOccupancePercentage = sessionMock.getPriceModifierPercentage().divide(BigDecimal.valueOf(100));
        BigDecimal modifier = BigDecimal.ONE.add(decimalOccupancePercentage);
        BigDecimal totalPaid = spotMock.getSector().getBasePrice().multiply(new BigDecimal("2.00"))
                .multiply(modifier).setScale(2, RoundingMode.HALF_UP);

        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(dto.licensePlate()))
                .thenReturn(Optional.of(sessionMock));

        parkingService.processExit(dto);

        assertEquals(sessionMock.getTotalPaid(), totalPaid);
        assertFalse(spotMock.getOccupied());
        verify(spotRepository).save(spotMock);
        verify(sessionRepository).save(sessionMock);
        assertNotNull(sessionMock.getExitTime());
    }

    @Test
    @DisplayName("Should return 0.00 when parked for less than 30 minutes")
    void processExitCase5() {

        var dto = new WebhookEventDTO("TEST", null,
                LocalDateTime.of(2026, 2, 2, 17, 20), null, null,
                EventType.EXIT);

        var sectorMock = new Sector();
        sectorMock.setName("A");
        sectorMock.setOpenHour(LocalTime.of(0,0));
        sectorMock.setCloseHour(LocalTime.of(23,59));
        sectorMock.setDurationLimitMinutes(1440);
        sectorMock.setBasePrice(new BigDecimal("40.50"));

        var spotMock = new Spot();
        spotMock.setSector(sectorMock);

        var sessionMock = new ParkingSession();
        sessionMock.setEntryTime(LocalDateTime.of(2026, 2, 2, 17, 0));
        sessionMock.setSpot(spotMock);
        sessionMock.setPriceModifierPercentage(new BigDecimal("25.00"));

        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(dto.licensePlate()))
                .thenReturn(Optional.of(sessionMock));

        parkingService.processExit(dto);

        assertEquals(BigDecimal.ZERO, sessionMock.getTotalPaid());
        assertFalse(spotMock.getOccupied());
        verify(spotRepository).save(spotMock);
        verify(sessionRepository).save(sessionMock);
    }
}