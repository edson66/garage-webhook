package br.com.estapar.garagewebhook.service;

import br.com.estapar.garagewebhook.domain.model.Sector;
import br.com.estapar.garagewebhook.domain.model.Spot;
import br.com.estapar.garagewebhook.dto.GarageSyncDTO;
import br.com.estapar.garagewebhook.repository.ParkingSessionRepository;
import br.com.estapar.garagewebhook.repository.SectorRepository;
import br.com.estapar.garagewebhook.repository.SpotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StartupSyncService implements CommandLineRunner {

    private final SectorRepository sectorRepository;
    private final SpotRepository spotRepository;
    private final ParkingSessionRepository parkingSessionRepository;

    private final RestClient restClient;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("Iniciando sincronização com o Simulador...");

        try {
            parkingSessionRepository.deleteAll();

            GarageSyncDTO response = restClient.get()
                    .uri("http://localhost:3000/garage")
                    .retrieve()
                    .body(GarageSyncDTO.class);

            if (response != null){

                List<Sector> sectors = response.sectors().stream().map(sec -> {
                    var sector = new Sector();
                    sector.setName(sec.name());
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

                System.out.println("Sincronização concluída! " + sectors.size() +
                        " setores e " + spots.size() + " vagas carregadas no banco.");
            }

        }catch (Exception e){
            System.err.println("Falha na sincronização. Certifique-se de que o Docker do simulador está rodando na porta 3000.");
            System.err.println("Detalhe do erro: " + e.getMessage());
        }

    }
}
