package com.railway.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign client to notify reservation-service after payment success/failure.
 * Calls POST /api/reservations/{reservationId}/payment-callback
 */
@FeignClient(name = "reservation-service")
public interface ReservationClient {

    @PostMapping("/api/reservations/{reservationId}/payment-callback")
    void sendPaymentCallback(
            @PathVariable("reservationId") Long reservationId,
            @RequestBody Map<String, String> payload
    );
}
