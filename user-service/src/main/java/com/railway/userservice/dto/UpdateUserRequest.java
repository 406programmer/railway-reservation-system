package com.railway.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Request body for PATCH /api/users/{id} – all fields optional.
 */
public record UpdateUserRequest(
        String name,

        @Email(message = "Must be a valid email")
        String email,

        @Size(min = 6, message = "Password must be at least 6 characters")
        String password
) {}
