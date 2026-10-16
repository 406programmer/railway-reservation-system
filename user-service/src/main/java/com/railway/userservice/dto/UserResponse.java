package com.railway.userservice.dto;

import com.railway.userservice.entity.User;

/**
 * Safe user view – never exposes the password hash.
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        String role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
