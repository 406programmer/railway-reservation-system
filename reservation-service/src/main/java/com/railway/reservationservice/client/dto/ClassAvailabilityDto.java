package com.railway.reservationservice.client.dto;

import java.math.BigDecimal;

/**
 * Mirrors train-service ClassAvailabilityResponse.
 */
public record ClassAvailabilityDto(
        String classType,
        long totalSeats,
        long availableSeats,
        BigDecimal fare
) {}
