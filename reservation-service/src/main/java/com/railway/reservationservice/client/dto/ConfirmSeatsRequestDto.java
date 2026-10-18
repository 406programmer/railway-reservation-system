package com.railway.reservationservice.client.dto;

import java.util.List;

/** Mirrors train-service ConfirmSeatsRequest. */
public record ConfirmSeatsRequestDto(Long scheduleId, List<Long> seatIds, Long reservationId) {}
