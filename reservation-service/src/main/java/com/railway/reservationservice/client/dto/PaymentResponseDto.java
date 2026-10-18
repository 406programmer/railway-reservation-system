package com.railway.reservationservice.client.dto;

import java.math.BigDecimal;

/**
 * Payment status/result response from payment-service.
 *
 * status values: SUCCESS, FAILED, PENDING, SERVICE_UNAVAILABLE
 */
public record PaymentResponseDto(
        String paymentId,
        Long reservationId,
        String status,
        String message,
        BigDecimal amount
) {}
