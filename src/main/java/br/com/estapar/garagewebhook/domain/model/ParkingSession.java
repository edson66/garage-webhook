package br.com.estapar.garagewebhook.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_sessions")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ParkingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String licensePlate;

    @Column(nullable = false)
    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    @ManyToOne
    @JoinColumn(name = "spot_id")
    private Spot spot;

    @Column(nullable = false)
    private BigDecimal priceModifierPercentage;

    private BigDecimal totalPaid;
}
