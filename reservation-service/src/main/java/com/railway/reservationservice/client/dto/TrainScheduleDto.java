package com.railway.reservationservice.client.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO mirroring train-service ScheduleResponse.
 * Fields: scheduleId, trainId, trainNumber, trainName,
 *         sourceStation, destinationStation, journeyDate,
 *         departureTime, arrivalTime, status, delayMinutes, platformNumber.
 */
public record TrainScheduleDto(
        Long scheduleId,
        Long trainId,
        String trainNumber,
        String trainName,
        String sourceStation,
        String destinationStation,
        LocalDate journeyDate,
        LocalTime departureTime,
        LocalTime arrivalTime,
        String status,
        Integer delayMinutes,
        String platformNumber
) {}
