package com.railway.reservationservice.client.dto;

import java.util.List;

/** Mirrors train-service ReleaseSeatsRequest. */
public record ReleaseSeatsRequestDto(Long scheduleId, List<Long> seatIds, Long reservationId) {}
