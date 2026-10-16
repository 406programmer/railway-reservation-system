package com.railway.trainservice.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReleaseSeatsRequest(
        @NotEmpty(message = "At least one seat ID must be provided")
        List<Long> seatIds,

        Long reservationId
) {}
