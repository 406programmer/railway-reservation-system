package com.railway.trainservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ConfirmSeatsRequest(
        @NotNull(message = "Schedule ID is required")
        Long scheduleId,

        @NotEmpty(message = "At least one seat ID must be provided")
        List<Long> seatIds,

        @NotNull(message = "Reservation ID is required to confirm booking")
        Long reservationId
) {}
