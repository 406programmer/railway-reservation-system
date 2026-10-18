package com.railway.reservationservice.client.dto;

import java.util.List;

/** Mirrors train-service ConfirmSeatsResponse. */
public record ConfirmSeatsResponseDto(Long scheduleId, List<Long> bookedSeatIds, Long reservationId, String message) {}
