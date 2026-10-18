package com.railway.reservationservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * Request body for POST /api/reservations.
 *
 * GDPR CHECKLIST for Reservation Creation
 * [x] Data minimisation — only required fields collected
 * [x] Legal basis: contract (user is purchasing a ticket)
 * [x] PII excluded from all log statements (see service layer)
 * [x] PII excluded from client-side storage (server-side only)
 * [x] Endpoint requires authentication
 * [x] Endpoint requires authorisation (ownership enforced in service)
 * [x] fare is NOT accepted from client — fetched from train-service
 * [x] Input validation implemented server-side
 */
public record CreateReservationRequest(

        @NotNull(message = "Schedule ID is required")
        Long scheduleId,

        @NotBlank(message = "Class type is required")
        @Pattern(regexp = "SL|3A|2A|1A", message = "classType must be one of: SL, 3A, 2A, 1A")
        String classType,

        @NotEmpty(message = "At least one passenger is required")
        @Size(min = 1, max = 6, message = "A reservation can have 1 to 6 passengers")
        @Valid
        List<PassengerDetailDto> passengers
) {}
