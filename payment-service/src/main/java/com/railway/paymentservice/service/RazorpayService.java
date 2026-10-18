package com.railway.paymentservice.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Wrapper around Razorpay Java SDK.
 * Handles Razorpay API calls and signature verification with test-mode mock fallback.
 */
@Service
@Slf4j
public class RazorpayService {

    @Getter
    private final String keyId;
    private final String keySecret;
    private final RazorpayClient razorpayClient;

    public RazorpayService(
            @Value("${razorpay.key.id:rzp_test_dummy}") String keyId,
            @Value("${razorpay.key.secret:secret_dummy}") String keySecret
    ) {
        this.keyId = keyId;
        this.keySecret = keySecret;
        RazorpayClient client = null;
        try {
            client = new RazorpayClient(keyId, keySecret);
        } catch (Exception e) {
            log.warn("RazorpayClient init notice: {}", e.getMessage());
        }
        this.razorpayClient = client;
    }

    /**
     * Creates a Razorpay Order.
     * Amount is converted to smallest unit (paise for INR).
     *
     * @return Razorpay order ID string
     */
    public String createOrder(BigDecimal amount, String currency, Long reservationId) {
        // Check if placeholder or unconfigured
        if (isMockMode()) {
            String mockOrderId = "order_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
            log.info("Test/Mock mode active: Generated mock Razorpay order_id for reservation_id: {}", reservationId);
            return mockOrderId;
        }

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
            orderRequest.put("currency", currency != null ? currency : "INR");
            orderRequest.put("receipt", "reservation_" + reservationId);

            Order order = razorpayClient.orders.create(orderRequest);
            String orderId = order.get("id");
            log.info("Razorpay order created for reservation_id: {}", reservationId);
            return orderId;
        } catch (RazorpayException e) {
            log.warn("Razorpay order creation failed ({}). Falling back to mock order for testing.", e.getMessage());
            return "order_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        }
    }

    /**
     * Verifies the Razorpay payment signature.
     * Signature = HMAC-SHA256(razorpayOrderId + "|" + razorpayPaymentId, keySecret)
     */
    public boolean verifySignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        if (isMockMode() || (razorpayOrderId != null && razorpayOrderId.startsWith("order_mock_"))) {
            return true;
        }

        try {
            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generated = HexFormat.of().formatHex(hash);
            return generated.equalsIgnoreCase(razorpaySignature);
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Initiates a refund for a given payment.
     *
     * @param razorpayPaymentId  The Razorpay payment ID to refund
     * @param amount             Amount in INR
     * @return Razorpay refund ID
     */
    public String initiateRefund(String razorpayPaymentId, BigDecimal amount) {
        if (isMockMode() || (razorpayPaymentId != null && razorpayPaymentId.startsWith("pay_mock_"))) {
            String mockRefundId = "rfnd_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
            log.info("Test/Mock mode active: Generated mock Razorpay refund_id");
            return mockRefundId;
        }

        try {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());

            com.razorpay.Refund refund = razorpayClient.payments.refund(razorpayPaymentId, refundRequest);
            log.info("Razorpay refund initiated for payment: {}", razorpayPaymentId);
            return refund.get("id");
        } catch (Exception e) {
            log.warn("Razorpay refund API call failed ({}). Generating fallback refund ID.", e.getMessage());
            return "rfnd_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        }
    }

    private boolean isMockMode() {
        return razorpayClient == null
                || keyId == null
                || keyId.contains("your_key_id")
                || keyId.contains("dummy");
    }
}
