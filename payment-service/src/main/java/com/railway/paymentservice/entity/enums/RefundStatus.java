package com.railway.paymentservice.entity.enums;

public enum RefundStatus {
    PENDING,   // Refund initiated with Razorpay
    SUCCESS,   // Refund processed successfully
    FAILED     // Refund failed
}
