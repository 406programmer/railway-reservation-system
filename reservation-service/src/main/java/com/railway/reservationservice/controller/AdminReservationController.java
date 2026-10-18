package com.railway.reservationservice.controller;

import com.railway.reservationservice.dto.CancellationResponse;
import com.railway.reservationservice.dto.ReservationResponse;
import com.railway.reservationservice.entity.enums.BookingStatus;
import com.railway.reservationservice.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Admin management REST controller for reservations.
 * Requires ROLE_ADMIN authority.
 */
@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
@Tag(name = "Admin Reservations", description = "Admin endpoints for viewing and managing all reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "Search and view all reservations (paginated with optional status filter)")
    @GetMapping
    public ResponseEntity<Page<ReservationResponse>> getAllReservations(
            @RequestParam(required = false) BookingStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reservationService.getAllReservations(status, pageable));
    }

    @Operation(summary = "Admin force cancel any reservation")
    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<CancellationResponse> adminCancelReservation(
            @PathVariable Long reservationId,
            Authentication authentication) {
        return ResponseEntity.ok(reservationService.cancelReservation(reservationId, authentication.getName(), true));
    }
}
