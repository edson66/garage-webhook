package br.com.estapar.garagewebhook.controller;

import br.com.estapar.garagewebhook.dto.WebhookEventDTO;
import br.com.estapar.garagewebhook.service.ParkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final ParkingService parkingService;

    @PostMapping
    public ResponseEntity<Void> receiveEvent(@RequestBody WebhookEventDTO event){

        switch (event.eventType()) {

            case ENTRY -> parkingService.processEntry(event);
            case PARKED -> parkingService.processParked(event);
            case EXIT -> parkingService.processExit(event);

        }

        return ResponseEntity.ok().build();
    }

}
