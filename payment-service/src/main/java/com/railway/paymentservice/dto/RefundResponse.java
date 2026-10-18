package com.railway.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.railway.paymentservice.entity.Refund;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response for refund operations.
 * PRIVACY: No PII. Returns only refund metadata.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RefundResponse(
        Long refundId,
        Long paymentId,
        Long reservationId,
        @JsonProperty("refundAmount")
        BigDecimal amount,
        String status,
        String razorpayRefundId,
        String reason,
        String message,
        LocalDateTime createdAt
) {
    public static RefundResponse from(Refund r, Long reservationId, String message) {
        return new RefundResponse(
                r.getRefundId(),
                r.getPaymentId(),
                reservationId,
                r.getAmount(),
                r.getStatus().name(),
                r.getRazorpayRefundId(),
                r.getReason(),
                message,
                r.getCreatedAt()
        );
    }

    public static RefundResponse from(Refund r, Long reservationId) {
        return from(r, reservationId, "Refund processed successfully");
    }

    public static RefundResponse from(Refund r) {
        return from(r, null, "Refund details retrieved");
    }
}
