package com.railway.reservationservice.exception;

public class SeatAllocationException extends RuntimeException {
    public SeatAllocationException(String message) {
        super(message);
    }
}
