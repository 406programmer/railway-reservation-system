package com.railway.reservationservice.client;

import com.railway.reservationservice.client.dto.PaymentRequestDto;
import com.railway.reservationservice.client.dto.PaymentResponseDto;
import com.railway.reservationservice.client.dto.RefundRequestDto;
import com.railway.reservationservice.client.dto.RefundResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client for payment-service.
 *
 * Contract:
 *   POST /api/payments/process  → initiate payment
 *   POST /api/payments/refund   → initiate refund
 *   GET  /api/payments/{paymentId}/status → check payment status
 *
 * GDPR: No PII in payment requests — uses reservationId (surrogate) and amount.
 *       Legal basis: contract. DPA: dependent on payment provider integration.
 */
@FeignClient(name = "payment-service", fallback = PaymentClientFallback.class)
public interface PaymentClient {

    /**
     * Initiate a payment for a reservation.
     */
    @PostMapping("/api/payments/process")
    PaymentResponseDto processPayment(@RequestBody PaymentRequestDto request);

    /**
     * Initiate a full refund for a reservation.
     * Idempotent — safe to retry; payment-service must handle duplicate refund requests.
     */
    @PostMapping("/api/payments/refund")
    RefundResponseDto initiateRefund(@RequestBody RefundRequestDto request);

    /**
     * Query the current status of a payment.
     */
    @GetMapping("/api/payments/{paymentId}/status")
    PaymentResponseDto getPaymentStatus(@PathVariable("paymentId") String paymentId);
}
