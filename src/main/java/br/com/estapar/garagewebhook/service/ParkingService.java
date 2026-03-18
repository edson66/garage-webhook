package br.com.estapar.garagewebhook.service;

import br.com.estapar.garagewebhook.domain.exception.GarageFullException;
import br.com.estapar.garagewebhook.domain.model.ParkingSession;
import br.com.estapar.garagewebhook.domain.model.Spot;
import br.com.estapar.garagewebhook.dto.WebhookEventDTO;
import br.com.estapar.garagewebhook.repository.ParkingSessionRepository;
import br.com.estapar.garagewebhook.repository.SpotRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ParkingService {

    private final SpotRepository spotRepository;
    private final ParkingSessionRepository sessionRepository;

    @Transactional
    public void processEntry(WebhookEventDTO event) {

        long totalSpots = spotRepository.count();
        long occupiedSpots = spotRepository.countByOccupiedTrue();

        double occupancePercentage = ((double) occupiedSpots /totalSpots)*100;

        if (occupancePercentage >= 100.0) {
            throw new GarageFullException("Garagem lotada. Entrada bloqueada para a placa: " + event.licensePlate());
        }

        BigDecimal priceModifier = calcularModificadorDePreco(occupancePercentage);

        var session = new ParkingSession();
        session.setLicensePlate(event.licensePlate());
        session.setEntryTime(event.entryTime());
        session.setPriceModifierPercentage(priceModifier);

        sessionRepository.save(session);

        System.out.println("Entrada liberada para: " + event.licensePlate() + " | Modificador: " + priceModifier + "%");
    }

    @Transactional
    public void processParked(WebhookEventDTO event) {

        Spot spot = spotRepository.findByLatAndLng(event.lat(), event.lng())
                .orElseThrow(() -> new EntityNotFoundException("Vaga não encontrada para lat/lng: " + event.lat() + ", " + event.lng()));

        ParkingSession session = sessionRepository.findByLicensePlateAndExitTimeIsNull(event.licensePlate())
                .orElseThrow(() -> new EntityNotFoundException("Carro não encontrado ou já saiu: " + event.licensePlate()));

        spot.setOccupied(true);
        session.setSpot(spot);

        spotRepository.save(spot);
        sessionRepository.save(session);

        System.out.println("Veículo " + event.licensePlate() + " estacionou no Setor " + spot.getSector().getName());
    }

    @Transactional
    public void processExit(WebhookEventDTO event) {

        ParkingSession session = sessionRepository.findByLicensePlateAndExitTimeIsNull(event.licensePlate())
                .orElseThrow(() -> new EntityNotFoundException("Carro não encontrado ou já saiu: " + event.licensePlate()));

        long minutesParked = Duration.between(session.getEntryTime(), event.exitTime()).toMinutes();

        BigDecimal totalPaid = BigDecimal.ZERO;

        if (minutesParked > 30){

            long hoursToCharge = (long) Math.ceil(minutesParked / 60.0);

            BigDecimal basePrice = session.getSpot().getSector().getBasePrice();
            BigDecimal grossValue = basePrice.multiply(BigDecimal.valueOf(hoursToCharge));

            BigDecimal decimalOccupancePercentage = session.getPriceModifierPercentage().divide(BigDecimal.valueOf(100));
            BigDecimal occupancePercentage = BigDecimal.ONE.add(decimalOccupancePercentage);

            totalPaid = grossValue.multiply(occupancePercentage).setScale(2, RoundingMode.HALF_UP);

        }

        session.setExitTime(event.exitTime());
        session.setTotalPaid(totalPaid);

        var spot = session.getSpot();
        if (spot != null) {
            spot.setOccupied(false);
            spotRepository.save(spot);
        }

        sessionRepository.save(session);

        System.out.println("Veículo " + event.licensePlate() + " saiu. Tempo: " + minutesParked + " min. Valor pago: R$ " + totalPaid);
    }

    private BigDecimal calcularModificadorDePreco(double occupance) {
        if (occupance < 25.0) {
            return new BigDecimal("-10.00");
        } else if (occupance < 50.0) {
            return BigDecimal.ZERO;
        } else if (occupance < 75.0) {
            return new BigDecimal("10.00");
        } else {
            return new BigDecimal("25.00");
        }
    }
}
