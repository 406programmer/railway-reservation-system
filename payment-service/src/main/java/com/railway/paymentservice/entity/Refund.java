package com.railway.paymentservice.entity;

import com.railway.paymentservice.entity.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Stores refund records linked to a payment.
 * GDPR: No PII. Legal basis: contract (cancellation refund). Retention: per company policy.
 */
@Entity
@Table(
    name = "refunds",
    indexes = {
        @Index(name = "idx_refund_payment_id", columnList = "payment_id")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refundId;

    /** The original payment being refunded. */
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus status;

    /** Razorpay Refund ID — returned by Razorpay refund API. */
    @Column(name = "razorpay_refund_id", length = 50)
    private String razorpayRefundId;

    @Column(length = 255)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
