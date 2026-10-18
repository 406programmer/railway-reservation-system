package com.railway.reservationservice.client.dto;

import java.math.BigDecimal;

/** Refund initiation request. No PII — uses reservationId and paymentId as surrogates. */
public record RefundRequestDto(
        Long reservationId,
        String paymentId,
        BigDecimal refundAmount,
        String reason
) {}
