package com.railway.reservationservice.dto;

import jakarta.validation.constraints.*;

/**
 * Passenger details within a booking request.
 * GDPR: Collects name, age, gender — PII. Legal basis: contract. Minimised: yes.
 */
public record PassengerDetailDto(

        @NotBlank(message = "Passenger name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @NotNull(message = "Age is required")
        @Min(value = 1, message = "Age must be at least 1")
        @Max(value = 120, message = "Age must not exceed 120")
        Integer age,

        @NotBlank(message = "Gender is required")
        @Pattern(regexp = "MALE|FEMALE|OTHER", message = "Gender must be MALE, FEMALE, or OTHER")
        String gender
) {}
