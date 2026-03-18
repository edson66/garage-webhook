package br.com.estapar.garagewebhook.repository;

import br.com.estapar.garagewebhook.domain.model.ParkingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParkingSessionRepository extends JpaRepository<ParkingSession,Long> {

    Optional<ParkingSession> findByLicensePlateAndExitTimeIsNull(String licensePlate);
}
