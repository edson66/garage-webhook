package br.com.estapar.garagewebhook.service;

import br.com.estapar.garagewebhook.domain.model.Sector;
import br.com.estapar.garagewebhook.domain.model.Spot;
import br.com.estapar.garagewebhook.dto.GarageSyncDTO;
import br.com.estapar.garagewebhook.repository.ParkingSessionRepository;
import br.com.estapar.garagewebhook.repository.SectorRepository;
import br.com.estapar.garagewebhook.repository.SpotRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@Slf4j
public class StartupSyncService implements CommandLineRunner {

    private final SectorRepository sectorRepository;
    private final SpotRepository spotRepository;
    private final ParkingSessionRepository parkingSessionRepository;

    private final RestClient restClient;

    private final String simulatorUrl;

    public StartupSyncService(SectorRepository sectorRepository, SpotRepository spotRepository,
                              ParkingSessionRepository parkingSessionRepository,
                              RestClient restClient,@Value("${simulator.url:http://localhost:3000/garage}") String simulatorUrl) {
        this.sectorRepository = sectorRepository;
        this.spotRepository = spotRepository;
        this.parkingSessionRepository = parkingSessionRepository;
        this.restClient = restClient;
        this.simulatorUrl = simulatorUrl;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Iniciando sincronização com o simulador...");

        try {
            parkingSessionRepository.deleteAll();

            GarageSyncDTO response = restClient.get()
                    .uri(simulatorUrl)
                    .retrieve()
                    .body(GarageSyncDTO.class);

            if (response != null){

                List<Sector> sectors = response.sectors().stream().map(sec -> {
                    var sector = new Sector();
                    sector.setName(sec.name().toUpperCase());
                    sector.setBasePrice(sec.basePrice());
                    sector.setMaxCapacity(sec.maxCapacity());
                    sector.setOpenHour(sec.openHour());
                    sector.setCloseHour(sec.closeHour());
                    sector.setDurationLimitMinutes(sec.durationLimitMinutes());

                    return sector;
                }).toList();

                sectorRepository.saveAll(sectors);

                List<Spot> spots = response.spots().stream().map(spt -> {
                    var spot = new Spot();
                    spot.setId(spt.id());
                    spot.setLat(spt.lat());
                    spot.setLng(spt.lng());
                    spot.setOccupied(spt.occupied());

                    var sectorRef = new Sector();
                    sectorRef.setName(spt.sector());
                    spot.setSector(sectorRef);

                    return spot;
                }).toList();

                spotRepository.saveAll(spots);

                log.info("Sincronização concluída! {} setores e {} vagas carregadas no banco",
                        sectors.size(),spots.size());
            }

        }catch (Exception e){
            log.error("Falha na sincronização. Certifique-se de que o Docker do simulador está rodando em {}.\nDetalhe: {}",simulatorUrl, e.getMessage());
        }

    }
}
