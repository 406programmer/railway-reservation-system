package com.railway.reservationservice.controller;

import com.railway.reservationservice.dto.CancellationResponse;
import com.railway.reservationservice.dto.CreateReservationRequest;
import com.railway.reservationservice.dto.PaymentCallbackRequest;
import com.railway.reservationservice.dto.ReservationResponse;
import com.railway.reservationservice.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Passenger and Client REST controller for reservations.
 *
 * GDPR CHECKLIST for Endpoints:
 * [x] Data minimisation applied — only necessary fields returned
 * [x] Legal basis identified and stored
 * [x] PII excluded from all log statements
 * [x] Endpoint requires authentication (except payment callback)
 * [x] Ownership checks enforced in service
 * [x] Response serialisers used
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation", description = "Ticket booking, PNR inquiry, payment callback, and cancellation")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "Create a reservation (holds seats for 5 minutes)")
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody CreateReservationRequest request,
            Authentication authentication) {
        ReservationResponse response = reservationService.createReservation(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get list of all reservations made by the authenticated user")
    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(Authentication authentication) {
        return ResponseEntity.ok(reservationService.getUserReservations(authentication.getName()));
    }

    @Operation(summary = "Get reservation details by reservation ID (enforces ownership)")
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> getReservationById(
            @PathVariable Long reservationId,
            Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(reservationService.getReservationById(
                reservationId,
                authentication != null ? authentication.getName() : null,
                isAdmin
        ));
    }

    @Operation(summary = "Get reservation details by PNR (enforces ownership or admin role)")
    @GetMapping("/pnr/{pnr}")
    public ResponseEntity<ReservationResponse> getReservationByPnr(
            @PathVariable String pnr,
            Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(reservationService.getReservationByPnr(
                pnr,
                authentication != null ? authentication.getName() : null,
                isAdmin
        ));
    }

    @Operation(summary = "Cancel a reservation and release seats (triggers refund if confirmed)")
    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<CancellationResponse> cancelReservation(
            @PathVariable Long reservationId,
            Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(reservationService.cancelReservation(
                reservationId,
                authentication != null ? authentication.getName() : null,
                isAdmin
        ));
    }

    @Operation(summary = "Payment callback invoked by payment-service after transaction")
    @PostMapping("/{reservationId}/payment-callback")
    public ResponseEntity<ReservationResponse> paymentCallback(
            @PathVariable Long reservationId,
            @RequestBody PaymentCallbackRequest request) {
        return ResponseEntity.ok(reservationService.handlePaymentCallback(reservationId, request));
    }
}
