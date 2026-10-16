package com.railway.trainservice.dto;

import com.railway.trainservice.entity.enums.CoachType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ClassFareRequest(
        @NotNull(message = "Class type is required")
        CoachType classType,

        @DecimalMin(value = "0.0", message = "Base fare cannot be negative")
        BigDecimal baseFare,

        @NotNull(message = "Fare per km is required")
        @DecimalMin(value = "0.01", message = "Fare per km must be greater than zero")
        BigDecimal farePerKm
) {}
