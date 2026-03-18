package br.com.estapar.garagewebhook.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "spots")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Spot {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sector_name", nullable = false)
    private Sector sector;

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal lat;

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal lng;

    private Boolean occupied = false;
}
