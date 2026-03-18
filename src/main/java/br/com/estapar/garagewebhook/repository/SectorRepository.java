package br.com.estapar.garagewebhook.repository;

import br.com.estapar.garagewebhook.domain.model.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectorRepository extends JpaRepository<Sector,String> {
}
