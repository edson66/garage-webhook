package br.com.estapar.garagewebhook.controller;

import br.com.estapar.garagewebhook.domain.enums.EventType;
import br.com.estapar.garagewebhook.dto.WebhookEventDTO;
import br.com.estapar.garagewebhook.service.ParkingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ParkingService parkingService;

    @Test
    @DisplayName("Should route ENTRY event to processEntry and return 200 OK")
    void receiveEvent_Entry() throws Exception {
        var dto = new WebhookEventDTO("ABC1234", LocalDateTime.now(), null,
                null, null, EventType.ENTRY);

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(parkingService).processEntry(any(WebhookEventDTO.class));
    }

    @Test
    @DisplayName("Should route PARKED event to processParked and return 200 OK")
    void receiveEvent_Parked() throws Exception {
        var dto = new WebhookEventDTO("ABC1234", null, null,
                new BigDecimal("-23.0"), new BigDecimal("-46.0"), EventType.PARKED);

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(parkingService).processParked(any(WebhookEventDTO.class));
    }

    @Test
    @DisplayName("Should route EXIT event to processExit and return 200 OK")
    void receiveEvent_Exit() throws Exception {
        var dto = new WebhookEventDTO("ABC1234", null, LocalDateTime.now(),
                null, null, EventType.EXIT);

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(parkingService).processExit(any(WebhookEventDTO.class));
    }
}