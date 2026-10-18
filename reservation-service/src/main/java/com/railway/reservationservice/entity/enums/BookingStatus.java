package com.railway.reservationservice.entity.enums;

/**
 * Lifecycle states of a reservation.
 *
 * State transitions:
 *   [start] → PENDING_PAYMENT  (created, seats held for 5 min)
 *   PENDING_PAYMENT → CONFIRMED        (payment success + seats confirmed)
 *   PENDING_PAYMENT → PAYMENT_FAILED   (payment gateway rejected)
 *   PENDING_PAYMENT → EXPIRED          (hold expired before payment)
 *   CONFIRMED → CANCELLATION_PENDING   (cancellation initiated)
 *   CANCELLATION_PENDING → CANCELLED   (seats released + refund done)
 *   CANCELLATION_PENDING → REFUND_PENDING (seats released, refund deferred)
 *   REFUND_PENDING → CANCELLED         (refund retry succeeded)
 */
public enum BookingStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    PAYMENT_FAILED,
    EXPIRED,
    CANCELLATION_PENDING,
    REFUND_PENDING,
    CANCELLED
}
