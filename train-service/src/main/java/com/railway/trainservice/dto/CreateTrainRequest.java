package com.railway.trainservice.dto;

import com.railway.trainservice.entity.enums.TrainType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTrainRequest(
        @NotBlank(message = "Train number is required")
        String trainNumber,

        @NotBlank(message = "Train name is required")
        String trainName,

        @NotNull(message = "Train type is required")
        TrainType trainType,

        @NotBlank(message = "Source station is required")
        String sourceStation,

        @NotBlank(message = "Destination station is required")
        String destinationStation,

        @NotNull(message = "Distance in km is required")
        @Positive(message = "Distance must be positive")
        Integer distanceKm
) {}
