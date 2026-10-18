package com.railway.reservationservice.client.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Mirrors train-service HoldSeatsResponse. */
public record HoldSeatsResponseDto(
        Long scheduleId,
        List<Long> heldSeatIds,
        LocalDateTime holdExpiry,
        String message
) {}
