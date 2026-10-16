package com.railway.trainservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.railway.trainservice.entity.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "journey_seats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"schedule_id", "seat_id"})
}, indexes = {
    @Index(name = "idx_js_schedule_status", columnList = "schedule_id, status"),
    @Index(name = "idx_js_hold_expiry", columnList = "status, hold_expiry")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JourneySeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "journey_seat_id")
    private Long journeySeatId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    @JsonIgnore
    private TrainSchedule schedule;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Column(name = "hold_expiry")
    private LocalDateTime holdExpiry;

    /**
     * Stored as plain ID referencing booking/reservation service (no cross-service DB FK).
     */
    @Column(name = "reservation_id")
    private Long reservationId;
}
