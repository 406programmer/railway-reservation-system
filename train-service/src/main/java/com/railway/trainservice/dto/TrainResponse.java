package com.railway.trainservice.dto;

import com.railway.trainservice.entity.Train;
import com.railway.trainservice.entity.enums.TrainType;

public record TrainResponse(
        Long trainId,
        String trainNumber,
        String trainName,
        TrainType trainType,
        String sourceStation,
        String destinationStation,
        Integer distanceKm,
        int totalCoaches
) {
    public static TrainResponse from(Train train) {
        return new TrainResponse(
                train.getTrainId(),
                train.getTrainNumber(),
                train.getTrainName(),
                train.getTrainType(),
                train.getSourceStation(),
                train.getDestinationStation(),
                train.getDistanceKm(),
                train.getCoaches() != null ? train.getCoaches().size() : 0
        );
    }
}
