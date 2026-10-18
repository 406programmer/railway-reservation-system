package com.railway.paymentservice.controller;

import com.railway.paymentservice.dto.PaymentRequest;
import com.railway.paymentservice.dto.PaymentResponse;
import com.railway.paymentservice.dto.RefundRequest;
import com.railway.paymentservice.dto.RefundResponse;
import com.railway.paymentservice.dto.VerifyPaymentRequest;
import com.railway.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for payment and refund operations.
 *
 * PRIVACY: All endpoints return minimal DTOs with surrogate IDs only.
 * Auth required: Yes (enforced by SecurityConfig & JwtAuthFilter).
 * PII fields: None.
 * Minimised: Yes.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    // PRIVACY: Returns paymentId, reservationId, amount, currency, status, razorpayOrderId, razorpayKeyId. Auth required: yes. PII fields: none. Minimised: yes.
    @PostMapping(path = {"/process", "/initiate"})
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        log.info("REST request to initiate payment for reservation_id: {}", request.reservationId());
        PaymentResponse response = paymentService.initiatePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PRIVACY: Returns payment verification result. Auth required: yes. PII fields: none. Minimised: yes.
    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(@Valid @RequestBody VerifyPaymentRequest request) {
        log.info("REST request to verify payment for razorpay_order_id: {}", request.razorpayOrderId());
        PaymentResponse response = paymentService.verifyPayment(request);
        return ResponseEntity.ok(response);
    }

    // PRIVACY: Simulates payment outcome. Auth required: yes. PII fields: none. Minimised: yes.
    @PostMapping("/{paymentId}/mock")
    public ResponseEntity<PaymentResponse> mockPayment(
            @PathVariable Long paymentId,
            @RequestParam(defaultValue = "SUCCESS") String status
    ) {
        log.info("REST request to mock payment outcome for payment_id: {}, status: {}", paymentId, status);
        PaymentResponse response = paymentService.mockPayment(paymentId, status);
        return ResponseEntity.ok(response);
    }

    // PRIVACY: Returns refund details. Auth required: yes. PII fields: none. Minimised: yes.
    @PostMapping("/refund")
    public ResponseEntity<RefundResponse> processRefund(@Valid @RequestBody RefundRequest request) {
        log.info("REST request to process refund for payment_id: {}, reservation_id: {}",
                request.paymentId(), request.reservationId());
        RefundResponse response = paymentService.processRefund(request);
        return ResponseEntity.ok(response);
    }

    // PRIVACY: Returns status of a payment. Auth required: yes. PII fields: none. Minimised: yes.
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable Long paymentId) {
        log.info("REST request to get status for payment_id: {}", paymentId);
        PaymentResponse response = paymentService.getPaymentStatus(paymentId);
        return ResponseEntity.ok(response);
    }

    // PRIVACY: Returns payment by reservation surrogate. Auth required: yes. PII fields: none. Minimised: yes.
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<PaymentResponse> getPaymentByReservationId(@PathVariable Long reservationId) {
        log.info("REST request to get payment for reservation_id: {}", reservationId);
        PaymentResponse response = paymentService.getPaymentByReservationId(reservationId);
        return ResponseEntity.ok(response);
    }

    // PRIVACY: Returns status of a refund. Auth required: yes. PII fields: none. Minimised: yes.
    @GetMapping("/refund/{refundId}")
    public ResponseEntity<RefundResponse> getRefundStatus(@PathVariable Long refundId) {
        log.info("REST request to get status for refund_id: {}", refundId);
        RefundResponse response = paymentService.getRefundStatus(refundId);
        return ResponseEntity.ok(response);
    }

    // PRIVACY: Returns refund linked to a payment. Auth required: yes. PII fields: none. Minimised: yes.
    @GetMapping("/{paymentId}/refund")
    public ResponseEntity<RefundResponse> getRefundByPaymentId(@PathVariable Long paymentId) {
        log.info("REST request to get refund for payment_id: {}", paymentId);
        RefundResponse response = paymentService.getRefundByPaymentId(paymentId);
        return ResponseEntity.ok(response);
    }
}
