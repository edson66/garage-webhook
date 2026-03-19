package br.com.estapar.garagewebhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GarageSyncDTO(
        @JsonProperty("garage")
        List<SectorDTO> sectors,
        List<SpotDTO> spots
) {
}
