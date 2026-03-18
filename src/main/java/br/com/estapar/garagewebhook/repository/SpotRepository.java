package br.com.estapar.garagewebhook.repository;

import br.com.estapar.garagewebhook.domain.model.Spot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

public interface SpotRepository extends JpaRepository<Spot,Long> {

    Optional<Spot> findByLatAndLng(BigDecimal lat, BigDecimal lng);

    long countByOccupiedTrue();
}
