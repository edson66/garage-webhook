package br.com.estapar.garagewebhook.controller;

import br.com.estapar.garagewebhook.dto.RevenueRequestDTO;
import br.com.estapar.garagewebhook.dto.RevenueResponseDTO;
import br.com.estapar.garagewebhook.service.RevenueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RevenueController.class)
class RevenueControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    RevenueService revenueService;

    @Test
    @DisplayName("Should return 200 OK and revenue data when request is valid")
    void getRevenue_Success() throws Exception {
        var requestDto = new RevenueRequestDTO(LocalDate.of(2026, 3, 20), "A");
        var expectedResponse = new RevenueResponseDTO(new BigDecimal("150.50"), "BRL", Instant.now());

        when(revenueService.calculateRevenue(any(RevenueRequestDTO.class))).thenReturn(expectedResponse);

        mockMvc.perform(get("/revenue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(150.50))
                .andExpect(jsonPath("$.currency").value("BRL"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when validation fails")
    void getRevenue_ValidationError() throws Exception {
        String invalidJsonPayload = "{}";

        mockMvc.perform(get("/revenue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJsonPayload))
                .andExpect(status().isBadRequest());
    }
}