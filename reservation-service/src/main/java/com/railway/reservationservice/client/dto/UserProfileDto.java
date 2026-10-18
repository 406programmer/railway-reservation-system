package com.railway.reservationservice.client.dto;

/** Mirrors user-service UserResponse (only fields we need). */
// PRIVACY: Returns id and email only. Auth required: yes. PII fields: email, name. Minimised: yes.
public record UserProfileDto(Long id, String name, String email, String role) {}
