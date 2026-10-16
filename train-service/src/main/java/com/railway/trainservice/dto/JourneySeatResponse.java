package com.railway.trainservice.dto;

import com.railway.trainservice.entity.JourneySeat;
import com.railway.trainservice.entity.enums.BerthType;
import com.railway.trainservice.entity.enums.CoachType;
import com.railway.trainservice.entity.enums.SeatStatus;

import java.time.LocalDateTime;

public record JourneySeatResponse(
        Long journeySeatId,
        Long seatId,
        String coachNumber,
        CoachType coachType,
        String seatNumber,
        BerthType berthType,
        SeatStatus status,
        LocalDateTime holdExpiry,
        Long reservationId
) {
    public static JourneySeatResponse from(JourneySeat js) {
        return new JourneySeatResponse(
                js.getJourneySeatId(),
                js.getSeat().getSeatId(),
                js.getSeat().getCoach().getCoachNumber(),
                js.getSeat().getCoach().getCoachType(),
                js.getSeat().getSeatNumber(),
                js.getSeat().getBerthType(),
                js.getStatus(),
                js.getHoldExpiry(),
                js.getReservationId()
        );
    }
}
