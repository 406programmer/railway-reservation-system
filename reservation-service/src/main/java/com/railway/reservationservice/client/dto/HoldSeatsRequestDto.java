package com.railway.reservationservice.client.dto;

import java.util.List;

/** Mirrors train-service HoldSeatsRequest. */
public record HoldSeatsRequestDto(List<Long> seatIds, Long reservationId) {}
