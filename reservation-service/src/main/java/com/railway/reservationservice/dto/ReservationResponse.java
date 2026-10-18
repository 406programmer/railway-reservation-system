package com.railway.reservationservice.dto;

import com.railway.reservationservice.entity.Reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Full reservation response returned to clients.
 * PRIVACY: Returns id, pnr, status, fare, passengers with seat assignments.
 *          Auth required: yes (ownership enforced). PII fields: passengerName (in nested list). Minimised: yes.
 */
public record ReservationResponse(
        Long reservationId,
        Long scheduleId,
        String pnr,
        String bookingStatus,
        Integer passengerCount,
        BigDecimal totalFare,
        LocalDateTime holdExpiresAt,
        LocalDateTime createdAt,
        List<PassengerResponse> passengers
) {
    public static ReservationResponse from(Reservation r, List<PassengerResponse> passengerResponses) {
        return new ReservationResponse(
                r.getReservationId(),
                r.getScheduleId(),
                r.getPnr(),
                r.getBookingStatus().name(),
                r.getPassengerCount(),
                r.getTotalFare(),
                r.getHoldExpiresAt(),
                r.getCreatedAt(),
                passengerResponses
        );
    }
}
