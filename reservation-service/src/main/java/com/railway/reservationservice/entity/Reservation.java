package com.railway.reservationservice.entity;

import com.railway.reservationservice.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate root for a train reservation.
 *
 * GDPR: Stores user_id (surrogate) and schedule_id — no direct PII fields here.
 *       PII (passenger names, ages) are in reservation_passengers.
 *       Legal basis: contract. Retention: per company policy.
 */
@Entity
@Table(
    name = "reservations",
    indexes = {
        @Index(name = "idx_res_user_id", columnList = "user_id"),
        @Index(name = "idx_res_schedule_id", columnList = "schedule_id"),
        @Index(name = "idx_res_status", columnList = "booking_status")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    /** External reference to user-service — no cross-service DB FK. */
    @Column(nullable = false)
    private Long userId;

    /** External reference to train-service schedule — no cross-service DB FK. */
    @Column(nullable = false)
    private Long scheduleId;

    /**
     * PNR — unique, 10-digit numeric string.
     * Null until status transitions to CONFIRMED.
     * Uniqueness enforced at DB level.
     */
    @Column(unique = true, length = 20)
    private String pnr;

    @Column(length = 64)
    private String paymentId;

    @Column(nullable = false)
    private Integer passengerCount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFare;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BookingStatus bookingStatus;

    /** Timestamp when the seat hold expires (5 minutes after creation). */
    private LocalDateTime holdExpiresAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime cancelledAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationPassenger> passengers = new ArrayList<>();
}
