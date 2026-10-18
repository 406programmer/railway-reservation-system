package com.railway.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to verify a Razorpay payment after frontend checkout completes.
 * These 3 fields are returned by Razorpay JS SDK after successful payment.
 */
public record VerifyPaymentRequest(

        @NotBlank(message = "razorpayPaymentId is required")
        String razorpayPaymentId,

        @NotBlank(message = "razorpayOrderId is required")
        String razorpayOrderId,

        @NotBlank(message = "razorpaySignature is required")
        String razorpaySignature
) {}
