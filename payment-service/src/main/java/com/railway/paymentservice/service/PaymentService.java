package com.railway.paymentservice.service;

import com.railway.paymentservice.dto.PaymentRequest;
import com.railway.paymentservice.dto.PaymentResponse;
import com.railway.paymentservice.dto.RefundRequest;
import com.railway.paymentservice.dto.RefundResponse;
import com.railway.paymentservice.dto.VerifyPaymentRequest;

/**
 * Service contract for payments and refunds.
 * GDPR: Processes financial transactions using surrogate reservation IDs only.
 * Legal basis: contract. DPA: Razorpay.
 */
public interface PaymentService {

    /**
     * Initiates a payment: creates Razorpay order and persists Payment entity in INITIATED state.
     */
    PaymentResponse initiatePayment(PaymentRequest request);

    /**
     * Verifies Razorpay signature and updates payment status to SUCCESS or FAILED.
     * Dispatches callback to reservation-service.
     */
    PaymentResponse verifyPayment(VerifyPaymentRequest request);

    /**
     * Explicit mock payment feature to simulate SUCCESS or FAILED outcome in test mode.
     * Dispatches callback to reservation-service.
     */
    PaymentResponse mockPayment(Long paymentId, String status);

    /**
     * Processes full/partial refund for an eligible successful payment.
     */
    RefundResponse processRefund(RefundRequest request);

    /**
     * Retrieves current status for a given payment ID.
     */
    PaymentResponse getPaymentStatus(Long paymentId);

    /**
     * Retrieves payment by reservation ID surrogate.
     */
    PaymentResponse getPaymentByReservationId(Long reservationId);

    /**
     * Retrieves refund status by refund ID.
     */
    RefundResponse getRefundStatus(Long refundId);

    /**
     * Retrieves refund details for a payment ID.
     */
    RefundResponse getRefundByPaymentId(Long paymentId);
}
