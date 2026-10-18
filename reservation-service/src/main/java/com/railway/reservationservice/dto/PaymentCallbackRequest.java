package com.railway.reservationservice.dto;

/**
 * Payment callback request from payment-service.
 * Used for the POST /api/reservations/{reservationId}/payment-callback endpoint.
 * GDPR: No PII — uses paymentId (surrogate) and status only.
 */
public record PaymentCallbackRequest(
        String paymentId,
        Long reservationId,
        String status,    // SUCCESS or FAILED
        String message
) {}
