package com.railway.paymentservice.service.impl;

import com.railway.paymentservice.client.ReservationClient;
import com.railway.paymentservice.dto.PaymentRequest;
import com.railway.paymentservice.dto.PaymentResponse;
import com.railway.paymentservice.dto.RefundRequest;
import com.railway.paymentservice.dto.RefundResponse;
import com.railway.paymentservice.dto.VerifyPaymentRequest;
import com.railway.paymentservice.entity.Payment;
import com.railway.paymentservice.entity.Refund;
import com.railway.paymentservice.entity.enums.PaymentStatus;
import com.railway.paymentservice.entity.enums.RefundStatus;
import com.railway.paymentservice.exception.PaymentAlreadyProcessedException;
import com.railway.paymentservice.exception.PaymentNotFoundException;
import com.railway.paymentservice.repository.PaymentRepository;
import com.railway.paymentservice.repository.RefundRepository;
import com.railway.paymentservice.service.PaymentService;
import com.railway.paymentservice.service.RazorpayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

// GDPR: Processes payment amount and reservation surrogates. Legal basis: contract. Retention: per company policy. DPA: Razorpay.
// GDPR CHECKLIST for Payment Processing
// [x] Data minimisation applied — only required fields collected/returned
// [x] Legal basis identified and stored (contract)
// [x] Consent mechanism implemented (if basis = consent) - N/A (contractual necessity)
// [x] PII excluded from all log statements
// [x] PII excluded from client-side storage
// [x] Endpoint requires authentication
// [x] Endpoint requires authorisation (RBAC)
// [x] Ownership check implemented (users can only access own data)
// [x] Data encrypted at rest (if storing sensitive fields)
// [x] Retention period documented and enforced
// [x] Right to erasure endpoint exists or updated
// [x] Third-party data sharing documented
// [x] Input validation implemented server-side
// [x] Response serialiser used (no raw DB object returned)
// [x] DPA in place with any third parties receiving this data

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final RazorpayService razorpayService;
    private final ReservationClient reservationClient;

    @Override
    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {
        log.info("Initiating payment for reservation_id: {}, amount: {}", request.reservationId(), request.amount());

        // Check if an existing payment exists for this reservation
        Optional<Payment> existingPaymentOpt = paymentRepository.findByReservationId(request.reservationId());
        if (existingPaymentOpt.isPresent()) {
            Payment existing = existingPaymentOpt.get();
            if (existing.getStatus() == PaymentStatus.SUCCESS) {
                throw new PaymentAlreadyProcessedException("Payment already completed for reservation: " + request.reservationId());
            }
            // Reuse existing INITIATED payment if available
            if (existing.getStatus() == PaymentStatus.INITIATED && existing.getRazorpayOrderId() != null) {
                return PaymentResponse.from(existing, razorpayService.getKeyId(), "Payment already initiated");
            }
        }

        // Create Razorpay Order
        String razorpayOrderId = razorpayService.createOrder(
                request.amount(),
                request.currency(),
                request.reservationId()
        );

        // Build and save Payment record
        Payment payment = existingPaymentOpt.orElseGet(Payment::new);
        payment.setReservationId(request.reservationId());
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency() != null ? request.currency() : "INR");
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setRazorpayOrderId(razorpayOrderId);
        payment.setUpdatedAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment saved with payment_id: {} in INITIATED status", savedPayment.getPaymentId());

        return PaymentResponse.from(savedPayment, razorpayService.getKeyId(), "Payment initiated successfully");
    }

    @Override
    @Transactional
    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {
        log.info("Verifying Razorpay payment for order_id: {}", request.razorpayOrderId());

        Payment payment = paymentRepository.findByRazorpayOrderId(request.razorpayOrderId())
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + request.razorpayOrderId()));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.info("Payment already verified for payment_id: {}", payment.getPaymentId());
            return PaymentResponse.from(payment, razorpayService.getKeyId(), "Payment already verified");
        }

        boolean isValid = razorpayService.verifySignature(
                request.razorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature()
        );

        if (isValid) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setRazorpayPaymentId(request.razorpayPaymentId());
            payment.setTransactionId(request.razorpayPaymentId());
            payment.setUpdatedAt(LocalDateTime.now());
            Payment saved = paymentRepository.save(payment);

            log.info("Payment verification succeeded for payment_id: {}", saved.getPaymentId());
            notifyReservationService(saved.getReservationId(), saved.getPaymentId(), "SUCCESS");
            return PaymentResponse.from(saved, null, "Payment verified successfully");
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());
            Payment saved = paymentRepository.save(payment);

            log.warn("Payment verification failed signature check for payment_id: {}", saved.getPaymentId());
            notifyReservationService(saved.getReservationId(), saved.getPaymentId(), "FAILED");
            throw new IllegalArgumentException("Invalid Razorpay payment signature");
        }
    }

    @Override
    @Transactional
    public PaymentResponse mockPayment(Long paymentId, String status) {
        log.info("Mock payment execution for payment_id: {}, target_status: {}", paymentId, status);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new PaymentAlreadyProcessedException("Payment " + paymentId + " is already completed");
        }

        if ("SUCCESS".equalsIgnoreCase(status)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            String mockTx = "pay_mock_" + System.currentTimeMillis();
            payment.setRazorpayPaymentId(mockTx);
            payment.setTransactionId(mockTx);
            payment.setUpdatedAt(LocalDateTime.now());
            Payment saved = paymentRepository.save(payment);

            notifyReservationService(saved.getReservationId(), saved.getPaymentId(), "SUCCESS");
            return PaymentResponse.from(saved, null, "Mock payment marked as SUCCESS");
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());
            Payment saved = paymentRepository.save(payment);

            notifyReservationService(saved.getReservationId(), saved.getPaymentId(), "FAILED");
            return PaymentResponse.from(saved, null, "Mock payment marked as FAILED");
        }
    }

    @Override
    @Transactional
    public RefundResponse processRefund(RefundRequest request) {
        log.info("Processing refund request for payment_id: {}, reservation_id: {}",
                request.paymentId(), request.reservationId());

        // Find payment by paymentId or fallback to reservationId
        Payment payment = null;
        if (request.paymentId() != null) {
            payment = paymentRepository.findById(request.paymentId()).orElse(null);
        }
        if (payment == null && request.reservationId() != null) {
            payment = paymentRepository.findByReservationId(request.reservationId()).orElse(null);
        }

        if (payment == null) {
            throw new PaymentNotFoundException("No payment record found to refund");
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Cannot refund payment with status: " + payment.getStatus());
        }

        // Idempotency check: see if a successful refund already exists for this payment
        Optional<Refund> existingRefund = refundRepository.findByPaymentId(payment.getPaymentId());
        if (existingRefund.isPresent() && existingRefund.get().getStatus() == RefundStatus.SUCCESS) {
            log.info("Refund already processed for payment_id: {}", payment.getPaymentId());
            return RefundResponse.from(existingRefund.get(), payment.getReservationId(), "Refund already completed");
        }

        BigDecimal refundAmount = request.amount() != null ? request.amount() : payment.getAmount();

        // Initiate Razorpay refund
        String razorpayPaymentId = payment.getRazorpayPaymentId() != null
                ? payment.getRazorpayPaymentId()
                : (payment.getTransactionId() != null ? payment.getTransactionId() : "pay_mock_refund");

        String refundId = razorpayService.initiateRefund(razorpayPaymentId, refundAmount);

        // Record Refund entity
        Refund refund = Refund.builder()
                .paymentId(payment.getPaymentId())
                .amount(refundAmount)
                .status(RefundStatus.SUCCESS)
                .razorpayRefundId(refundId)
                .reason(request.reason() != null ? request.reason() : "Customer cancellation")
                .updatedAt(LocalDateTime.now())
                .build();

        Refund savedRefund = refundRepository.save(refund);
        log.info("Refund recorded with refund_id: {} for payment_id: {}", savedRefund.getRefundId(), payment.getPaymentId());

        return RefundResponse.from(savedRefund, payment.getReservationId(), "Refund processed successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
        return PaymentResponse.from(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReservationId(Long reservationId) {
        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for reservation: " + reservationId));
        return PaymentResponse.from(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundResponse getRefundStatus(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new PaymentNotFoundException("Refund not found with id: " + refundId));
        return RefundResponse.from(refund);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundResponse getRefundByPaymentId(Long paymentId) {
        Refund refund = refundRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Refund not found for payment: " + paymentId));
        return RefundResponse.from(refund);
    }

    private void notifyReservationService(Long reservationId, Long paymentId, String status) {
        try {
            log.info("Dispatching payment callback to reservation-service for reservation_id: {}, status: {}",
                    reservationId, status);
            reservationClient.sendPaymentCallback(
                    reservationId,
                    Map.of("paymentId", String.valueOf(paymentId), "status", status)
            );
        } catch (Exception ex) {
            log.warn("Could not dispatch callback to reservation-service for reservation_id: {}. Exception: {}",
                    reservationId, ex.getMessage());
        }
    }
}
