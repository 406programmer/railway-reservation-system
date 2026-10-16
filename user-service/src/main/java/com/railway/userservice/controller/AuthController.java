package com.railway.userservice.controller;

import com.railway.userservice.dto.AuthResponse;
import com.railway.userservice.dto.LoginRequest;
import com.railway.userservice.dto.RegisterRequest;
import com.railway.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public auth endpoints – no JWT required.
 *
 *  POST /api/auth/register  → register a new account
 *  POST /api/auth/login     → login and receive a JWT
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * Register a new user.
     *
     * @param request  { name, email, password, role? }
     * @return 201 Created with JWT + user details
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(request));
    }

    /**
     * Login with email + password.
     *
     * @param request  { email, password }
     * @return 200 OK with JWT + user details
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }
}
