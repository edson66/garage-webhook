package br.com.estapar.garagewebhook.controller;

import br.com.estapar.garagewebhook.dto.RevenueRequestDTO;
import br.com.estapar.garagewebhook.dto.RevenueResponseDTO;
import br.com.estapar.garagewebhook.service.RevenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/revenue")
@RequiredArgsConstructor
public class RevenueController {

    private final RevenueService service;

    @GetMapping
    public ResponseEntity<RevenueResponseDTO> getRevenue(@RequestBody @Valid RevenueRequestDTO data){

        var response = service.calculateRevenue(data);

        return ResponseEntity.ok(response);
    }
}
