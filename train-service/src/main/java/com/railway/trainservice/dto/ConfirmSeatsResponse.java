package com.railway.trainservice.dto;

import java.util.List;

public record ConfirmSeatsResponse(
        Long scheduleId,
        List<Long> bookedSeatIds,
        Long reservationId,
        String message
) {}
