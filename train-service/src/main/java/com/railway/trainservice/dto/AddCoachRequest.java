package com.railway.trainservice.dto;

import com.railway.trainservice.entity.enums.CoachType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddCoachRequest(
        @NotBlank(message = "Coach number is required (e.g. S1, B1, A1)")
        String coachNumber,

        @NotNull(message = "Coach type is required")
        CoachType coachType,

        /**
         * Optional: if omitted or 0, default seats are generated based on coach type:
         * SL: 72 seats, AC3: 64 seats, AC2: 48 seats, AC1: 24 seats, CC: 78 seats, GEN: 100 seats
         */
        @Positive(message = "Total seats must be positive")
        Integer totalSeats
) {}
