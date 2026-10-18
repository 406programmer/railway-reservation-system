package com.railway.reservationservice.client.dto;

import java.time.LocalDateTime;

/**
 * Mirrors train-service JourneySeatResponse.
 */
public record JourneySeatDto(
        Long journeySeatId,
        Long seatId,
        String coachNumber,
        String coachType,
        String seatNumber,
        String berthType,
        String status,
        LocalDateTime holdExpiry,
        Long reservationId
) {}
