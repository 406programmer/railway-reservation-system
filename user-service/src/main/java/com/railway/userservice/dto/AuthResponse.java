package com.railway.userservice.dto;

/**
 * Response returned after a successful login or register.
 */
public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMs,
        UserResponse user
) {
    public AuthResponse(String token, long expiresInMs, UserResponse user) {
        this(token, "Bearer", expiresInMs, user);
    }
}
