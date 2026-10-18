package com.railway.trainservice.controller;

import com.railway.trainservice.dto.*;
import com.railway.trainservice.entity.enums.CoachType;
import com.railway.trainservice.entity.enums.SeatStatus;
import com.railway.trainservice.service.SeatBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal REST controller for seat inventory operations.
 * These endpoints are called by Reservation Service — NOT by end users.

 * Access is restricted by a shared internal secret header:
 *   X-Internal-Secret: <value from application.properties>

 * Endpoints:
 *   GET    /internal/schedules/{scheduleId}/seats              → view seats
 *   POST   /internal/schedules/{scheduleId}/seat-holds         → hold seats
 *   POST   /internal/reservations/{reservationId}/seat-holds/confirm → confirm hold
 *   POST   /internal/reservations/{reservationId}/seats/release → release seats
 *   GET    /internal/reservations/{reservationId}/seat-assignments → get assigned seats
 */
@RestController
@Tag(name = "Internal Seat Inventory", description = "Service-to-service seat hold and inventory APIs")
public class SeatController {

    private final SeatBookingService seatBookingService;

    @Value("${internal.api.secret}")
    private String internalSecret;

    public SeatController(SeatBookingService seatBookingService) {
        this.seatBookingService = seatBookingService;
    }

    // ── Auth helper ─────────────────────────────────────────────────────────

    private void validateInternalSecret(String secret) {
        if (!internalSecret.equals(secret)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Invalid internal API secret");
        }
    }

    // ── Seat query ──────────────────────────────────────────────────────────

    @Operation(summary = "List journey seats for a schedule (Internal)")
    @GetMapping("/internal/schedules/{scheduleId}/seats")
    public ResponseEntity<List<JourneySeatResponse>> getSeats(
            @RequestHeader("X-Internal-Secret") String secret,
            @PathVariable Long scheduleId,
            @RequestParam(required = false) CoachType coachType,
            @RequestParam(required = false) SeatStatus status) {
        validateInternalSecret(secret);
        return ResponseEntity.ok(seatBookingService.getSeatsForSchedule(scheduleId, coachType, status));
    }

    // ── Seat hold ───────────────────────────────────────────────────────────

    @Operation(summary = "Hold seats for a pending booking (Internal)")
    @PostMapping("/internal/schedules/{scheduleId}/seat-holds")
    public ResponseEntity<HoldSeatsResponse> holdSeats(
            @RequestHeader("X-Internal-Secret") String secret,
            @PathVariable Long scheduleId,
            @Valid @RequestBody HoldSeatsRequest request) {
        validateInternalSecret(secret);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(seatBookingService.holdSeats(scheduleId, request));
    }

    // ── Seat confirm ────────────────────────────────────────────────────────

    @Operation(summary = "Confirm held seats after successful payment (Internal)")
    @PostMapping("/internal/reservations/{reservationId}/seat-holds/confirm")
    public ResponseEntity<ConfirmSeatsResponse> confirmSeats(
            @RequestHeader("X-Internal-Secret") String secret,
            @PathVariable Long reservationId,
            @Valid @RequestBody ConfirmSeatsRequest request) {
        validateInternalSecret(secret);
        // scheduleId must be provided in the request body
        return ResponseEntity.ok(
                seatBookingService.confirmBooking(request.scheduleId(), request));
    }

    // ── Seat release ────────────────────────────────────────────────────────

    @Operation(summary = "Release seats back to available (Internal)")
    @PostMapping("/internal/reservations/{reservationId}/seats/release")
    public ResponseEntity<Void> releaseSeats(
            @RequestHeader("X-Internal-Secret") String secret,
            @PathVariable Long reservationId,
            @Valid @RequestBody ReleaseSeatsRequest request) {
        validateInternalSecret(secret);
        seatBookingService.releaseSeats(request.scheduleId(), request);
        return ResponseEntity.noContent().build();
    }

    // ── Seat assignments ────────────────────────────────────────────────────

    @Operation(summary = "Get seat assignments for a reservation (Internal)")
    @GetMapping("/internal/reservations/{reservationId}/seat-assignments")
    public ResponseEntity<List<JourneySeatResponse>> getSeatAssignments(
            @RequestHeader("X-Internal-Secret") String secret,
            @PathVariable Long reservationId) {
        validateInternalSecret(secret);
        return ResponseEntity.ok(seatBookingService.getJourneySeatsByReservation(reservationId));
    }

}
