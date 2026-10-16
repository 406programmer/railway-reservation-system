package com.railway.userservice.controller;

import com.railway.userservice.dto.UpdateUserRequest;
import com.railway.userservice.dto.UserResponse;
import com.railway.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Protected user-profile endpoints – JWT required.
 *
 *  GET    /api/users/me          → get own profile
 *  PATCH  /api/users/me         → update own profile
 *  GET    /api/users/{id}       → get user by ID (own or admin)
 *  GET    /api/users/email/{email} → get user by email (own or admin)
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get currently authenticated user's profile.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserByEmail(userDetails.getUsername()));
    }

    /**
     * Update currently authenticated user's profile.
     */
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse current = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(userService.updateUser(current.id(), request));
    }

    /**
     * Get any user by ID. Regular users can only access their own profile.
     * Admins can access any profile.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse own = userService.getUserByEmail(userDetails.getUsername());

        // Non-admins can only see themselves
        if (!own.id().equals(id) && !userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Get user by email – admin only.
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #email == authentication.name")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }
}
