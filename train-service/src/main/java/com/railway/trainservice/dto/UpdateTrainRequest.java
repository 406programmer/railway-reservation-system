package com.railway.trainservice.dto;

import com.railway.trainservice.entity.enums.TrainType;
import jakarta.validation.constraints.Positive;

public record UpdateTrainRequest(
        String trainName,
        TrainType trainType,
        String sourceStation,
        String destinationStation,
        @Positive(message = "Distance must be positive")
        Integer distanceKm
) {}
