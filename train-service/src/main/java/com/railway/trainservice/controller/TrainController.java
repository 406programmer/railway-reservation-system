package com.railway.trainservice.controller;

import com.railway.trainservice.dto.*;
import com.railway.trainservice.service.TrainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only REST controller for Train master data management.
 *
 * Endpoints:
 *   POST   /api/admin/trains                          → create a train
 *   GET    /api/admin/trains                          → list all trains
 *   GET    /api/trains/{trainId}                      → get train (passenger + admin)
 *   PUT    /api/admin/trains/{trainId}                → update train details
 *   PATCH  /api/admin/trains/{trainId}/deactivate     → deactivate a train
 *   POST   /api/admin/trains/{trainId}/coaches        → add a coach
 *   GET    /api/admin/trains/{trainId}/coaches        → list coaches
 *   GET    /api/admin/coaches/{coachId}/seats         → list seats in a coach
 *   PUT    /api/admin/fares/{trainId}/{classType}     → set/update class fare
 *   GET    /api/admin/fares                           → list all fares for a train
 *   GET    /api/fares                                 → passenger fare lookup
 */
@RestController
@Tag(name = "Train Management", description = "Train, coach, seat and fare management APIs")
public class TrainController {

    private final TrainService trainService;

    public TrainController(TrainService trainService) {
        this.trainService = trainService;
    }

    // ── Admin: Train CRUD ───────────────────────────────────────────────────

    @Operation(summary = "Create a new train (Admin)")
    @PostMapping("/api/admin/trains")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TrainResponse> createTrain(@Valid @RequestBody CreateTrainRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainService.createTrain(request));
    }

    @Operation(summary = "List all trains (Admin)")
    @GetMapping("/api/admin/trains")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<TrainResponse>> getAllTrains() {
        return ResponseEntity.ok(trainService.getAllTrains());
    }

    @Operation(summary = "Update train details (Admin)")
    @PutMapping("/api/admin/trains/{trainId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TrainResponse> updateTrain(
            @PathVariable Long trainId,
            @Valid @RequestBody UpdateTrainRequest request) {
        return ResponseEntity.ok(trainService.updateTrain(trainId, request));
    }

    @Operation(summary = "Deactivate a train (Admin)")
    @PatchMapping("/api/admin/trains/{trainId}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deactivateTrain(@PathVariable Long trainId) {
        trainService.deactivateTrain(trainId);
        return ResponseEntity.noContent().build();
    }

    // ── Passenger: Train lookup ─────────────────────────────────────────────

    @Operation(summary = "Get train by ID (Passenger + Admin)")
    @GetMapping("/api/trains/{trainId}")
    public ResponseEntity<TrainResponse> getTrainById(@PathVariable Long trainId) {
        return ResponseEntity.ok(trainService.getTrainById(trainId));
    }

    // ── Admin: Coach management ─────────────────────────────────────────────

    @Operation(summary = "Add a coach to a train (Admin)")
    @PostMapping("/api/admin/trains/{trainId}/coaches")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CoachResponse> addCoach(
            @PathVariable Long trainId,
            @Valid @RequestBody AddCoachRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trainService.addCoach(trainId, request));
    }

    @Operation(summary = "List coaches for a train (Admin)")
    @GetMapping("/api/admin/trains/{trainId}/coaches")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<CoachResponse>> getCoaches(@PathVariable Long trainId) {
        return ResponseEntity.ok(trainService.getCoachesByTrain(trainId));
    }

    @Operation(summary = "List seats in a coach (Admin)")
    @GetMapping("/api/admin/coaches/{coachId}/seats")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<SeatResponse>> getSeats(@PathVariable Long coachId) {
        return ResponseEntity.ok(trainService.getSeatsByCoach(coachId));
    }

    // ── Admin: Fare management ──────────────────────────────────────────────

    @Operation(summary = "Set / update class fare for a train (Admin)")
    @PutMapping("/api/admin/fares/{trainId}/{classType}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ClassFareResponse> configureFare(
            @PathVariable Long trainId,
            @PathVariable String classType,
            @Valid @RequestBody ClassFareRequest request) {
        return ResponseEntity.ok(trainService.configureFare(trainId, request));
    }

    @Operation(summary = "List all fares for a train (Admin)")
    @GetMapping("/api/admin/fares")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ClassFareResponse>> getAllFares(@RequestParam Long trainId) {
        return ResponseEntity.ok(trainService.getFaresByTrain(trainId));
    }

    // ── Passenger: Fare lookup ──────────────────────────────────────────────

    @Operation(summary = "Get fare for a train and class (Passenger)")
    @GetMapping("/api/fares")
    public ResponseEntity<List<ClassFareResponse>> getFares(@RequestParam Long trainId) {
        return ResponseEntity.ok(trainService.getFaresByTrain(trainId));
    }
}
