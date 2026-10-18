package com.railway.reservationservice.dto;

import com.railway.reservationservice.entity.ReservationSeat;

import java.time.LocalDateTime;

/**
 * Confirmed seat assignment snapshot — returned only once seat is confirmed by train-service.
 */
public record SeatAssignmentResponse(
        Long journeySeatId,
        String coachNumber,
        String seatNumber,
        String classType,
        LocalDateTime assignedAt
) {
    public static SeatAssignmentResponse from(ReservationSeat seat) {
        if (seat == null) return null;
        return new SeatAssignmentResponse(
                seat.getJourneySeatId(),
                seat.getCoachNumber(),
                seat.getSeatNumber(),
                seat.getClassType(),
                seat.getAssignedAt()
        );
    }
}
