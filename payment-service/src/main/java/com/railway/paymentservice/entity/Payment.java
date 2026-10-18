package com.railway.paymentservice.entity;

import com.railway.paymentservice.entity.enums.PaymentStatus;
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
 * Stores payment records.
 * GDPR: Stores reservationId (surrogate), amount, status, and Razorpay/transaction references.
 * No PII stored. Legal basis: contract. Retention: per company policy.
 */
@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_pay_reservation_id", columnList = "reservation_id"),
        @Index(name = "idx_pay_status", columnList = "status")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    /** Foreign reference to reservation-service — no cross-DB FK. */
    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    /** Razorpay Order ID — created when payment is initiated. */
    @Column(name = "razorpay_order_id", unique = true, length = 50)
    private String razorpayOrderId;

    /** Razorpay Payment ID — populated after user completes checkout. */
    @Column(name = "razorpay_payment_id", length = 50)
    private String razorpayPaymentId;

    /** Transaction ID / mock transaction reference */
    @Column(name = "transaction_id", length = 64)
    private String transactionId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
