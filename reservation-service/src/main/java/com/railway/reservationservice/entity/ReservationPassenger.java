package com.railway.reservationservice.entity;

import com.railway.reservationservice.entity.enums.PassengerStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One passenger within a reservation.
 *
 * GDPR: Stores passenger_name, age, gender — PII fields.
 *       Legal basis: contract. Retention: per company policy.
 *       DPA: N/A (internal DB).
 */
@Entity
@Table(
    name = "reservation_passengers",
    indexes = {
        @Index(name = "idx_pass_reservation_id", columnList = "reservation_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class ReservationPassenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationPassengerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    // GDPR: PII fields — name, age, gender. Only stored as necessary for the booking contract.
    @Column(nullable = false, length = 100)
    private String passengerName;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false, length = 20)
    private String gender;

    /** Per-passenger fare snapshot at booking time. Never recalculated after booking. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fareAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PassengerStatus passengerStatus;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /** Seat assignment — null until Train Service confirms the hold. */
    @OneToOne(mappedBy = "passenger", cascade = CascadeType.ALL, orphanRemoval = true)
    private ReservationSeat seat;
}
