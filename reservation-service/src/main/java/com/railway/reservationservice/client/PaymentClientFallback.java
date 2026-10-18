package com.railway.reservationservice.client;

import com.railway.reservationservice.client.dto.PaymentRequestDto;
import com.railway.reservationservice.client.dto.PaymentResponseDto;
import com.railway.reservationservice.client.dto.RefundRequestDto;
import com.railway.reservationservice.client.dto.RefundResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback for PaymentClient when payment-service is unavailable.
 * Returns a response indicating the service is down — the caller must handle this gracefully.
 */
@Component
@Slf4j
public class PaymentClientFallback implements PaymentClient {

    @Override
    public PaymentResponseDto processPayment(PaymentRequestDto request) {
        log.error("PaymentClient fallback: payment-service is unavailable. reservationId={}", request.reservationId());
        return new PaymentResponseDto(null, request.reservationId(), "SERVICE_UNAVAILABLE",
                "Payment service is currently unavailable. Please try again later.", null);
    }

    @Override
    public RefundResponseDto initiateRefund(RefundRequestDto request) {
        log.error("PaymentClient fallback: payment-service unavailable for refund. reservationId={}", request.reservationId());
        return new RefundResponseDto(null, request.reservationId(), "SERVICE_UNAVAILABLE",
                "Refund service is currently unavailable. Refund will be retried.", null);
    }

    @Override
    public PaymentResponseDto getPaymentStatus(String paymentId) {
        log.error("PaymentClient fallback: cannot fetch payment status. paymentId={}", paymentId);
        return new PaymentResponseDto(paymentId, null, "SERVICE_UNAVAILABLE",
                "Payment service is currently unavailable.", null);
    }
}
