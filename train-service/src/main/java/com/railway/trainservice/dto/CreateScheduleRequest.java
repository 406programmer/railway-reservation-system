package com.railway.trainservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateScheduleRequest(
        @NotNull(message = "Train ID is required")
        Long trainId,

        @NotNull(message = "Journey date is required")
        @FutureOrPresent(message = "Journey date cannot be in the past")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate journeyDate,

        @NotNull(message = "Departure time is required")
        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime departureTime,

        @NotNull(message = "Arrival time is required")
        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime arrivalTime,

        String platformNumber
) {}
