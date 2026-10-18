package com.railway.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request to initiate a payment.
 * GDPR: No PII — uses reservationId (surrogate) and amount only.
 */
public record PaymentRequest(

        @NotNull(message = "Reservation ID is required")
        @JsonAlias({"reservation_id", "reservationId"})
        Long reservationId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.0", message = "Amount must be at least 1")
        BigDecimal amount,

        String currency,

        String description
) {
    public PaymentRequest {
        if (currency == null || currency.isBlank()) {
            currency = "INR";
        }
    }
}
