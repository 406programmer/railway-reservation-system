package com.railway.reservationservice.dto;

import java.time.LocalDateTime;

/**
 * Response after a cancellation is initiated.
 */
public record CancellationResponse(
        Long reservationId,
        String pnr,
        String bookingStatus,
        String message,
        LocalDateTime cancelledAt
) {}
