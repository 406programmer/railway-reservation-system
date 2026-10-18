package com.railway.paymentservice.entity.enums;

public enum PaymentStatus {
    INITIATED,   // Razorpay order created, awaiting user payment
    SUCCESS,     // Payment verified and confirmed
    FAILED       // Payment failed or verification failed
}
