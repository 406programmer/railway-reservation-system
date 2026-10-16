package com.railway.trainservice.dto;

import com.railway.trainservice.entity.TrainSchedule;
import com.railway.trainservice.entity.enums.ScheduleStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleResponse(
        Long scheduleId,
        Long trainId,
        String trainNumber,
        String trainName,
        String sourceStation,
        String destinationStation,
        LocalDate journeyDate,
        LocalTime departureTime,
        LocalTime arrivalTime,
        ScheduleStatus status,
        Integer delayMinutes,
        String platformNumber
) {
    public static ScheduleResponse from(TrainSchedule schedule) {
        return new ScheduleResponse(
                schedule.getScheduleId(),
                schedule.getTrain().getTrainId(),
                schedule.getTrain().getTrainNumber(),
                schedule.getTrain().getTrainName(),
                schedule.getTrain().getSourceStation(),
                schedule.getTrain().getDestinationStation(),
                schedule.getJourneyDate(),
                schedule.getDepartureTime(),
                schedule.getArrivalTime(),
                schedule.getStatus(),
                schedule.getDelayMinutes(),
                schedule.getPlatformNumber()
        );
    }
}
