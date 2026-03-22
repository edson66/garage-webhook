package br.com.estapar.garagewebhook.repository;

import br.com.estapar.garagewebhook.domain.model.ParkingSession;
import br.com.estapar.garagewebhook.domain.model.Sector;
import br.com.estapar.garagewebhook.domain.model.Spot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ParkingSessionRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    ParkingSessionRepository sessionRepository;

    @Autowired
    SpotRepository spotRepository;

    @Autowired
    SectorRepository sectorRepository;

    @BeforeEach
    void setUp(){
        sessionRepository.deleteAll();
        spotRepository.deleteAll();
        sectorRepository.deleteAll();

        var sectorA = new Sector();
        sectorA.setName("A");
        sectorA.setBasePrice(new BigDecimal("3.30"));
        sectorA.setOpenHour(LocalTime.of(6,0));
        sectorA.setCloseHour(LocalTime.of(0,0));
        sectorA.setMaxCapacity(5);
        sectorA.setDurationLimitMinutes(1000);
        sectorRepository.save(sectorA);

        var spot = new Spot();
        spot.setId(1L);
        spot.setSector(sectorA);
        spot.setLat(new BigDecimal("1.00"));
        spot.setLng(new BigDecimal("1.50"));
        spotRepository.save(spot);

        var session1 = new ParkingSession();
        session1.setLicensePlate("CAR-0001");
        session1.setSpot(spot);
        session1.setEntryTime(LocalDateTime.of(2026, 3, 20, 12, 0));
        session1.setExitTime(LocalDateTime.of(2026, 3, 20, 14, 0));
        session1.setTotalPaid(new BigDecimal("50.00"));
        session1.setPriceModifierPercentage(BigDecimal.ZERO);
        sessionRepository.save(session1);

        var session2 = new ParkingSession();
        session2.setLicensePlate("CAR-0002");
        session2.setSpot(spot);
        session2.setEntryTime(LocalDateTime.of(2026, 3, 20, 16, 0));
        session2.setExitTime(LocalDateTime.of(2026, 3, 20, 18, 0));
        session2.setTotalPaid(new BigDecimal("25.00"));
        session2.setPriceModifierPercentage(BigDecimal.ZERO);
        sessionRepository.save(session2);

        var session3 = new ParkingSession();
        session3.setLicensePlate("CAR-0003");
        session3.setSpot(spot);
        session3.setEntryTime(LocalDateTime.of(2026, 3, 20, 22, 0));
        session3.setExitTime(LocalDateTime.of(2026, 3, 21, 0, 0));
        session3.setTotalPaid(new BigDecimal("100.00"));
        session3.setPriceModifierPercentage(BigDecimal.ZERO);
        sessionRepository.save(session3);
    }

    @Test
    @DisplayName("Should sum totalPaid only for the specified period and sector")
    void sumRevenueByPeriod_Success() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 20, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 21, 0, 0);

        BigDecimal total = sessionRepository.sumRevenueByPeriod(start, end, "A");

        assertEquals(0, total.compareTo(new BigDecimal("75.00")));
    }

    @Test
    @DisplayName("Should return 0.00 (via COALESCE) when no cars exited on the date")
    void sumRevenueByPeriod_Empty() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 11, 0, 0);

        BigDecimal total = sessionRepository.sumRevenueByPeriod(start, end, "A");

        assertEquals(0, total.compareTo(new BigDecimal("0.00")));
    }

    @Test
    @DisplayName("Should return zero when sector does not match")
    void sumRevenueByPeriod_Wrong_Sector() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 20, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 21, 0, 0);

        BigDecimal total = sessionRepository.sumRevenueByPeriod(start, end, "B");

        assertEquals(0, total.compareTo(BigDecimal.ZERO));
    }
}