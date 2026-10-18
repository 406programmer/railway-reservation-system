package com.railway.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

/**
 * Request to initiate a refund.
 * Supports both paymentId and reservationId as lookup keys.
 * GDPR: No PII — uses surrogate IDs only.
 */
public record RefundRequest(

        @JsonAlias({"payment_id", "paymentId"})
        Long paymentId,

        @JsonAlias({"reservation_id", "reservationId"})
        Long reservationId,

        @JsonAlias({"refund_amount", "refundAmount", "amount"})
        @DecimalMin(value = "1.0", message = "Refund amount must be at least 1")
        BigDecimal amount,

        String reason
) {}
