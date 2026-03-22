package br.com.estapar.garagewebhook.service;

import br.com.estapar.garagewebhook.domain.model.Sector;
import br.com.estapar.garagewebhook.dto.GarageSyncDTO;
import br.com.estapar.garagewebhook.dto.SectorDTO;
import br.com.estapar.garagewebhook.dto.SpotDTO;
import br.com.estapar.garagewebhook.repository.ParkingSessionRepository;
import br.com.estapar.garagewebhook.repository.SectorRepository;
import br.com.estapar.garagewebhook.repository.SpotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartupSyncServiceTest {

    @Mock
    ParkingSessionRepository parkingSessionRepository;
    @Mock
    SpotRepository spotRepository;
    @Mock
    SectorRepository sectorRepository;
    @Mock
    RestClient.Builder restClientBuilder;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    RestClient restClient;

    @Captor
    ArgumentCaptor<List<Sector>> sectorListCaptor;

    StartupSyncService service;

    @BeforeEach
    void setUp() {
        when(restClientBuilder.build()).thenReturn(restClient);

        service = new StartupSyncService(
                sectorRepository,
                spotRepository,
                parkingSessionRepository,
                restClientBuilder.build(),
                "http://localhost:3000/garage"
        );
    }

    @Test
    @DisplayName("Should sync garage successfully when API returns valid data")
    void runCase1() throws Exception {
        var sectorDto = new SectorDTO("a", new BigDecimal("40.50"), 10,
                LocalTime.of(8, 0), LocalTime.of(18, 0), 60);
        var spotDto = new SpotDTO(1L, "a", new BigDecimal("-23.0"), new BigDecimal("-46.0"), false);
        var garageSyncDTO = new GarageSyncDTO(List.of(sectorDto), List.of(spotDto));

        when(restClient.get().uri(anyString()).retrieve().body(GarageSyncDTO.class))
                .thenReturn(garageSyncDTO);

        service.run();

        verify(parkingSessionRepository).deleteAll();
        verify(sectorRepository).saveAll(sectorListCaptor.capture());
        verify(spotRepository).saveAll(any());
        assertEquals("A", sectorListCaptor.getValue().get(0).getName());
        assertEquals(10, sectorListCaptor.getValue().get(0).getMaxCapacity());

    }

    @Test
    @DisplayName("Should clear sessions but not save anything if API returns null")
    void runCase2() throws Exception {
        when(restClient.get().uri(anyString()).retrieve().body(GarageSyncDTO.class))
                .thenReturn(null);

        service.run();

        verify(parkingSessionRepository).deleteAll();
        verify(sectorRepository, never()).saveAll(any());
        verify(spotRepository,never()).saveAll(any());

    }

    @Test
    @DisplayName("Should clear sessions and not crash if RestClient throws an exception")
    void runCase3() {
    when(restClient.get()).thenThrow(new RuntimeException("Conexão recusada!"));

        assertDoesNotThrow(() -> service.run());
        verify(parkingSessionRepository).deleteAll();
    }
}