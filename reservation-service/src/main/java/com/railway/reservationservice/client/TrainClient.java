package com.railway.reservationservice.client;

import com.railway.reservationservice.client.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feign client for train-service seat inventory operations.
 * Uses the internal secret header for authentication.
 * Base URL resolved via Eureka by service name "train-service".
 */
@FeignClient(name = "train-service", configuration = TrainClientConfig.class)
public interface TrainClient {

    /**
     * Fetch schedule details — validates status, journey date, timing.
     * PRIVACY: Returns non-PII schedule metadata. Auth required: JWT (via config). PII fields: none.
     */
    @GetMapping("/api/schedules/{scheduleId}")
    TrainScheduleDto getSchedule(@PathVariable("scheduleId") Long scheduleId);

    /**
     * Get class-level availability and fare for a specific coach type.
     * PRIVACY: Returns aggregate counts + fare. No PII.
     */
    @GetMapping("/api/schedules/{scheduleId}/availability")
    ClassAvailabilityDto getAvailability(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestParam("classType") String classType);

    /**
     * Get individual journey seats filtered by coach type and status.
     * Used to pick specific seat IDs for holding.
     */
    @GetMapping("/internal/schedules/{scheduleId}/seats")
    List<JourneySeatDto> getSeats(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestParam(value = "coachType", required = false) String coachType,
            @RequestParam(value = "status", required = false) String status);

    /**
     * Hold seats — atomically reserves seatIds for the given reservationId.
     */
    @PostMapping("/internal/schedules/{scheduleId}/seat-holds")
    HoldSeatsResponseDto holdSeats(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestBody HoldSeatsRequestDto request);

    /**
     * Confirm previously held seats after payment succeeds.
     */
    @PostMapping("/internal/reservations/{reservationId}/seat-holds/confirm")
    ConfirmSeatsResponseDto confirmSeats(
            @PathVariable("reservationId") Long reservationId,
            @RequestBody ConfirmSeatsRequestDto request);

    /**
     * Release seats — called on payment failure, hold expiry, or cancellation.
     */
    @PostMapping("/internal/reservations/{reservationId}/seats/release")
    void releaseSeats(
            @PathVariable("reservationId") Long reservationId,
            @RequestBody ReleaseSeatsRequestDto request);

    /**
     * Get confirmed seat assignments (coach + seat number) for a reservation.
     */
    @GetMapping("/internal/reservations/{reservationId}/seat-assignments")
    List<JourneySeatDto> getSeatAssignments(@PathVariable("reservationId") Long reservationId);
}
