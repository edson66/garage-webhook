package br.com.estapar.garagewebhook.service;

import br.com.estapar.garagewebhook.domain.exception.GarageFullException;
import br.com.estapar.garagewebhook.domain.exception.NotParkedException;
import br.com.estapar.garagewebhook.domain.model.ParkingSession;
import br.com.estapar.garagewebhook.domain.model.Sector;
import br.com.estapar.garagewebhook.domain.model.Spot;
import br.com.estapar.garagewebhook.dto.WebhookEventDTO;
import br.com.estapar.garagewebhook.repository.ParkingSessionRepository;
import br.com.estapar.garagewebhook.repository.SpotRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
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

        BigDecimal priceModifier = calculatePriceModifier(occupancePercentage);

        var session = new ParkingSession();
        session.setLicensePlate(event.licensePlate());
        session.setEntryTime(event.entryTime());
        session.setPriceModifierPercentage(priceModifier);

        sessionRepository.save(session);

        log.info("Entrada liberada para: {} | Modificador: {}%",event.licensePlate(),priceModifier);
    }

    @Transactional
    public void processParked(WebhookEventDTO event) {

        Spot spot = spotRepository.findByLatAndLng(event.lat(), event.lng())
                .orElseThrow(() -> new EntityNotFoundException("Vaga não encontrada para lat/lng: " + event.lat() + ", " + event.lng()));

        ParkingSession session = sessionRepository.findByLicensePlateAndExitTimeIsNull(event.licensePlate())
                .orElseThrow(() -> new EntityNotFoundException("Carro não encontrado ou já saiu: " + event.licensePlate()));

        if (spot.getOccupied()){
            log.warn("A vaga enviada já estava ocupada! Sobrescrevendo dados com novo carro...");
        }

        spot.setOccupied(true);
        session.setSpot(spot);

        spotRepository.save(spot);
        sessionRepository.save(session);

        log.info("Veículo {} estacionou no Setor {}", event.licensePlate(), spot.getSector().getName());

        verifyOperatingHours(session.getEntryTime(), spot.getSector(), event.licensePlate());
    }

    @Transactional
    public void processExit(WebhookEventDTO event) {

        ParkingSession session = sessionRepository.findByLicensePlateAndExitTimeIsNull(event.licensePlate())
                .orElseThrow(() -> new EntityNotFoundException("Carro não encontrado ou já saiu: " + event.licensePlate()));

        var spot = session.getSpot();
        if (spot == null){
            throw new NotParkedException("Veículo ainda não estacionado!");
        }

        long minutesParked = Duration.between(session.getEntryTime(), event.exitTime()).toMinutes();

        BigDecimal totalPaid = calculateTotalPaid(minutesParked,session);

        session.setExitTime(event.exitTime());
        session.setTotalPaid(totalPaid);

        spot.setOccupied(false);
        spotRepository.save(spot);

        sessionRepository.save(session);

        log.info("Veículo {} saiu. Tempo: {} min. Valor pago: R$ {}", event.licensePlate(),minutesParked, totalPaid);

        verifyDurationLimitMinutes(minutesParked,spot.getSector(), session.getLicensePlate());
        verifyOperatingHours(session.getExitTime(),spot.getSector(), session.getLicensePlate());
    }

    private BigDecimal calculateTotalPaid(long minutesParked,ParkingSession session){

        if (minutesParked > 30){

            long hoursToCharge = (long) Math.ceil(minutesParked / 60.0);

            BigDecimal basePrice = session.getSpot().getSector().getBasePrice();
            BigDecimal grossValue = basePrice.multiply(BigDecimal.valueOf(hoursToCharge));

            BigDecimal decimalOccupancePercentage = session.getPriceModifierPercentage().divide(BigDecimal.valueOf(100));
            BigDecimal occupancePercentage = BigDecimal.ONE.add(decimalOccupancePercentage);

            return grossValue.multiply(occupancePercentage).setScale(2, RoundingMode.HALF_UP);

        }

        return BigDecimal.ZERO;
    }

    private BigDecimal calculatePriceModifier(double occupance) {
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

    private void verifyDurationLimitMinutes(long minutesParked, Sector sector, String licensePlate){

        if (minutesParked > sector.getDurationLimitMinutes()){
            log.warn("ALERTA : O carro {} ficou {} min e extrapolou o limite do setor {} ({} min).",
                    licensePlate,minutesParked,sector.getName(),sector.getDurationLimitMinutes());
        }

    }

    private void verifyOperatingHours(LocalDateTime actionTime, Sector sector, String licensePlate) {
        LocalTime time = actionTime.toLocalTime();

        if (time.isBefore(sector.getOpenHour()) || time.isAfter(sector.getCloseHour())) {
            log.warn("ALERTA : O carro {} movimentou-se fora do horário comercial do setor {} (Abre: {} | Fecha {}).",
                    licensePlate,sector.getName(),sector.getOpenHour(),sector.getCloseHour());
        }
    }
}
