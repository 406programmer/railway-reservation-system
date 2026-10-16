package com.railway.trainservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.railway.trainservice.entity.enums.CoachType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "class_fares", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"train_id", "class_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassFare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fare_id")
    private Long fareId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_id", nullable = false)
    @JsonIgnore
    private Train train;

    @Enumerated(EnumType.STRING)
    @Column(name = "class_type", nullable = false, length = 20)
    private CoachType classType;

    @Column(name = "base_fare", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal baseFare = BigDecimal.ZERO;

    @Column(name = "fare_per_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal farePerKm;

    public BigDecimal calculateTotalFare(int distanceKm) {
        BigDecimal distanceFactor = farePerKm.multiply(BigDecimal.valueOf(distanceKm));
        return (baseFare != null ? baseFare : BigDecimal.ZERO)
                .add(distanceFactor)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
