package com.railway.reservationservice.dto;

import com.railway.reservationservice.entity.ReservationPassenger;

import java.math.BigDecimal;

/**
 * Per-passenger details in the reservation response.
 * PRIVACY: Returns passengerName, age, gender, fare, status, seat assignment. Minimised: yes.
 */
public record PassengerResponse(
        Long reservationPassengerId,
        String passengerName,
        Integer age,
        String gender,
        BigDecimal fareAmount,
        String passengerStatus,
        SeatAssignmentResponse seatAssignment
) {
    public static PassengerResponse from(ReservationPassenger p, SeatAssignmentResponse seat) {
        return new PassengerResponse(
                p.getReservationPassengerId(),
                p.getPassengerName(),
                p.getAge(),
                p.getGender(),
                p.getFareAmount(),
                p.getPassengerStatus().name(),
                seat
        );
    }
}
