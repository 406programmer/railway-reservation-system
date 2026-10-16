package com.railway.trainservice.dto;

import com.railway.trainservice.entity.enums.CoachType;

import java.math.BigDecimal;

public record ClassAvailabilityResponse(
        CoachType classType,
        long totalSeats,
        long availableSeats,
        BigDecimal fare
) {}
