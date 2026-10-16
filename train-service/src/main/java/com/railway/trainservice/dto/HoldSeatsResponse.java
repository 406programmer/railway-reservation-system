package com.railway.trainservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public record HoldSeatsResponse(
        Long scheduleId,
        List<Long> heldSeatIds,
        LocalDateTime holdExpiry,
        String message
) {}
