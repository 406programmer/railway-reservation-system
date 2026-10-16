package com.railway.trainservice.dto;

import com.railway.trainservice.entity.ClassFare;
import com.railway.trainservice.entity.enums.CoachType;

import java.math.BigDecimal;

public record ClassFareResponse(
        Long fareId,
        Long trainId,
        CoachType classType,
        BigDecimal baseFare,
        BigDecimal farePerKm,
        BigDecimal totalFare
) {
    public static ClassFareResponse from(ClassFare fare) {
        return new ClassFareResponse(
                fare.getFareId(),
                fare.getTrain().getTrainId(),
                fare.getClassType(),
                fare.getBaseFare(),
                fare.getFarePerKm(),
                fare.calculateTotalFare(fare.getTrain().getDistanceKm())
        );
    }
}
