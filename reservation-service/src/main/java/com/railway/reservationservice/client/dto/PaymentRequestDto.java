package com.railway.reservationservice.client.dto;

import java.math.BigDecimal;

/**
 * Payment initiation request.
 * GDPR: No PII — uses reservationId (surrogate) and amount only.
 */
public record PaymentRequestDto(
        Long reservationId,
        BigDecimal amount,
        String currency,
        String description
) {}
