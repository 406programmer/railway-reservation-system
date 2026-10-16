package com.railway.trainservice.dto;

import com.railway.trainservice.entity.Coach;
import com.railway.trainservice.entity.enums.CoachType;

public record CoachResponse(
        Long coachId,
        Long trainId,
        String coachNumber,
        CoachType coachType,
        Integer totalSeats
) {
    public static CoachResponse from(Coach coach) {
        return new CoachResponse(
                coach.getCoachId(),
                coach.getTrain().getTrainId(),
                coach.getCoachNumber(),
                coach.getCoachType(),
                coach.getTotalSeats()
        );
    }
}
