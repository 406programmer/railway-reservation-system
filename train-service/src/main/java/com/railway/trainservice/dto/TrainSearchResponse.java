package com.railway.trainservice.dto;

import com.railway.trainservice.entity.enums.ScheduleStatus;
import com.railway.trainservice.entity.enums.TrainType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record TrainSearchResponse(
        Long scheduleId,
        Long trainId,
        String trainNumber,
        String trainName,
        TrainType trainType,
        String sourceStation,
        String destinationStation,
        Integer distanceKm,
        LocalDate journeyDate,
        LocalTime departureTime,
        LocalTime arrivalTime,
        ScheduleStatus status,
        Integer delayMinutes,
        String platformNumber,
        List<ClassAvailabilityResponse> classes
) {}
