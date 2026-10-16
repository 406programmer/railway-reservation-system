package com.railway.trainservice.dto;

import com.railway.trainservice.entity.Seat;
import com.railway.trainservice.entity.enums.BerthType;
import com.railway.trainservice.entity.enums.CoachType;

public record SeatResponse(
        Long seatId,
        Long coachId,
        String coachNumber,
        CoachType coachType,
        String seatNumber,
        BerthType berthType
) {
    public static SeatResponse from(Seat seat) {
        return new SeatResponse(
                seat.getSeatId(),
                seat.getCoach().getCoachId(),
                seat.getCoach().getCoachNumber(),
                seat.getCoach().getCoachType(),
                seat.getSeatNumber(),
                seat.getBerthType()
        );
    }
}
