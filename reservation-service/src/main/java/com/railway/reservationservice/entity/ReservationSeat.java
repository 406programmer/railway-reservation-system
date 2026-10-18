package com.railway.reservationservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Snapshot of a confirmed seat assignment.
 * Created only after Train Service confirms the seat hold.
 * journey_seat_id is a plain Long — no cross-service DB FK.
 *
 * Preserved historically after cancellation (seat assignment record remains for audit).
 */
@Entity
@Table(
    name = "reservation_seats",
    indexes = {
        @Index(name = "idx_rs_passenger_id", columnList = "reservation_passenger_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class ReservationSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationSeatId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_passenger_id", nullable = false)
    private ReservationPassenger passenger;

    /** External reference to train-service JourneySeat — no cross-service DB FK. */
    @Column(nullable = false)
    private Long journeySeatId;

    /** Coach number snapshot (denormalized for display without re-querying train-service). */
    @Column(nullable = false, length = 10)
    private String coachNumber;

    /** Seat number snapshot. */
    @Column(nullable = false, length = 10)
    private String seatNumber;

    /** Class type snapshot (e.g. "SL", "AC3", "AC2", "AC1"). */
    @Column(nullable = false, length = 10)
    private String classType;

    @Column(nullable = false)
    private LocalDateTime assignedAt;
}
