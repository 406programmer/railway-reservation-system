package com.railway.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.railway.paymentservice.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response after initiating or checking a payment.
 * Contains Razorpay order details needed by the frontend to open checkout.
 * PRIVACY: No PII. Returns only payment metadata and surrogate identifiers.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(
        Long paymentId,
        Long reservationId,
        BigDecimal amount,
        String currency,
        String status,
        String razorpayOrderId,
        String razorpayKeyId,
        String transactionId,
        String message,
        LocalDateTime createdAt
) {
    public static PaymentResponse from(Payment p, String razorpayKeyId, String message) {
        String effectiveTxId = p.getTransactionId() != null ? p.getTransactionId() : p.getRazorpayPaymentId();
        return new PaymentResponse(
                p.getPaymentId(),
                p.getReservationId(),
                p.getAmount(),
                p.getCurrency(),
                p.getStatus().name(),
                p.getRazorpayOrderId(),
                razorpayKeyId,
                effectiveTxId,
                message,
                p.getCreatedAt()
        );
    }

    public static PaymentResponse from(Payment p, String razorpayKeyId) {
        return from(p, razorpayKeyId, null);
    }

    public static PaymentResponse from(Payment p) {
        return from(p, null, null);
    }
}
