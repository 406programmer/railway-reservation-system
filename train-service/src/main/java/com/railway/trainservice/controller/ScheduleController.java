package com.railway.trainservice.controller;

import com.railway.trainservice.dto.*;
import com.railway.trainservice.entity.enums.CoachType;
import com.railway.trainservice.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for schedule management and train search.
 *
 * Admin endpoints:
 *   POST   /api/admin/trains/{trainId}/schedules     → create schedule
 *   GET    /api/admin/schedules/{scheduleId}          → get schedule (admin)
 *   PATCH  /api/admin/schedules/{scheduleId}/cancel   → cancel a schedule
 *
 * Passenger endpoints:
 *   GET    /api/schedules/search                      → search trains by route + date
 *   GET    /api/schedules/{scheduleId}                → get schedule details
 *   GET    /api/schedules/{scheduleId}/availability   → class-level seat availability
 */
@RestController
@Tag(name = "Schedule Management", description = "Train schedule and seat availability APIs")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    // ── Admin: Schedule management ──────────────────────────────────────────

    @Operation(summary = "Create a journey schedule for a train (Admin)")
    @PostMapping("/api/admin/trains/{trainId}/schedules")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ScheduleResponse> createSchedule(
            @PathVariable Long trainId,
            @Valid @RequestBody CreateScheduleRequest request) {
        // trainId from path is already in the request body — validate they match
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scheduleService.createSchedule(request));
    }

    @Operation(summary = "Get a schedule by ID (Admin)")
    @GetMapping("/api/admin/schedules/{scheduleId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ScheduleResponse> getScheduleAdmin(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleService.getScheduleById(scheduleId));
    }

    @Operation(summary = "Cancel a journey schedule (Admin)")
    @PatchMapping("/api/admin/schedules/{scheduleId}/cancel")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ScheduleResponse> cancelSchedule(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleService.cancelSchedule(scheduleId));
    }

    // ── Passenger: Train search & schedule lookup ───────────────────────────

    @Operation(summary = "Search available trains by route and date (Passenger)")
    @GetMapping("/api/schedules/search")
    public ResponseEntity<List<TrainSearchResponse>> searchTrains(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(scheduleService.searchTrains(source, destination, date));
    }

    @Operation(summary = "Get schedule details by ID (Passenger)")
    @GetMapping("/api/schedules/{scheduleId}")
    public ResponseEntity<ScheduleResponse> getSchedule(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleService.getScheduleById(scheduleId));
    }

    @Operation(summary = "Get class-level seat availability for a schedule (Passenger)")
    @GetMapping("/api/schedules/{scheduleId}/availability")
    public ResponseEntity<ClassAvailabilityResponse> getAvailability(
            @PathVariable Long scheduleId,
            @RequestParam CoachType classType) {
        return ResponseEntity.ok(scheduleService.getClassAvailability(scheduleId, classType));
    }
}
