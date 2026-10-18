package com.railway.reservationservice.client.dto;

import java.math.BigDecimal;

/** Refund status response from payment-service. */
public record RefundResponseDto(
        String refundId,
        Long reservationId,
        String status,
        String message,
        BigDecimal refundAmount
) {}
