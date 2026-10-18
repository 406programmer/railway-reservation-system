package com.railway.trainservice.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record ReleaseSeatsRequest(
        @NotNull(message = "Schedule ID is required")
        Long scheduleId,

        @NotEmpty(message = "At least one seat ID must be provided")
        List<Long> seatIds,

        Long reservationId
) {}
