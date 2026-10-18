package com.railway.reservationservice.entity.enums;

/**
 * Per-passenger status mirroring the parent reservation status.
 */
public enum PassengerStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    PAYMENT_FAILED,
    EXPIRED,
    CANCELLED
}
