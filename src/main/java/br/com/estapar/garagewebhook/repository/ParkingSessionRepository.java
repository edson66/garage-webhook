package br.com.estapar.garagewebhook.repository;

import br.com.estapar.garagewebhook.domain.model.ParkingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface ParkingSessionRepository extends JpaRepository<ParkingSession,Long> {

    Optional<ParkingSession> findByLicensePlateAndExitTimeIsNull(String licensePlate);

    @Query("""
        SELECT COALESCE(SUM(p.totalPaid), 0)
            FROM ParkingSession p
                WHERE p.exitTime >= :start AND p.exitTime < :end
                    AND p.spot.sector.name = :sector
    """)
    BigDecimal sumRevenueByPeriod(LocalDateTime start, LocalDateTime end, String sector);
}
