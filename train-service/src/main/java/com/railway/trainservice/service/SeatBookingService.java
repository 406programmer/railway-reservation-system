package com.railway.trainservice.service;

import com.railway.trainservice.dto.*;
import com.railway.trainservice.entity.JourneySeat;
import com.railway.trainservice.entity.enums.CoachType;
import com.railway.trainservice.entity.enums.SeatStatus;
import com.railway.trainservice.exception.InvalidOperationException;
import com.railway.trainservice.exception.ResourceNotFoundException;
import com.railway.trainservice.exception.SeatUnavailableException;
import com.railway.trainservice.repository.JourneySeatRepository;
import com.railway.trainservice.repository.TrainScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatBookingService {

    private final JourneySeatRepository journeySeatRepository;
    private final TrainScheduleRepository scheduleRepository;

    @Value("${train.booking.seat-hold-duration-minutes:10}")
    private int holdDurationMinutes;

    @Transactional(readOnly = true)
    public List<JourneySeatResponse> getSeatsForSchedule(Long scheduleId, CoachType coachType, SeatStatus status) {
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new ResourceNotFoundException("Schedule", "id", scheduleId);
        }

        List<JourneySeat> seats;
        if (coachType != null) {
            seats = journeySeatRepository.findByScheduleScheduleIdAndSeatCoachCoachType(scheduleId, coachType);
        } else if (status != null) {
            seats = journeySeatRepository.findByScheduleScheduleIdAndStatus(scheduleId, status);
        } else {
            seats = journeySeatRepository.findByScheduleScheduleId(scheduleId);
        }

        return seats.stream().map(JourneySeatResponse::from).toList();
    }

    /**
     * Temporarily holds seats for payment processing.
     * Pessimistically locks seat records to prevent race conditions / double-booking.
     */
    @Transactional
    public HoldSeatsResponse holdSeats(Long scheduleId, HoldSeatsRequest request) {
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new ResourceNotFoundException("Schedule", "id", scheduleId);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusMinutes(holdDurationMinutes);
        List<Long> heldSeatIds = new ArrayList<>();

        for (Long seatId : request.seatIds()) {
            JourneySeat journeySeat = journeySeatRepository.findByScheduleIdAndSeatIdWithLock(scheduleId, seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat", "seatId", seatId));

            // Check if seat is already booked or actively held
            boolean isActivelyHeld = journeySeat.getStatus() == SeatStatus.HELD &&
                    journeySeat.getHoldExpiry() != null &&
                    journeySeat.getHoldExpiry().isAfter(now);

            if (journeySeat.getStatus() == SeatStatus.BOOKED || isActivelyHeld) {
                throw new SeatUnavailableException("Seat " + journeySeat.getSeat().getSeatNumber() +
                        " in coach " + journeySeat.getSeat().getCoach().getCoachNumber() +
                        " is already " + journeySeat.getStatus());
            }

            journeySeat.setStatus(SeatStatus.HELD);
            journeySeat.setHoldExpiry(expiry);
            journeySeat.setReservationId(request.reservationId());
            heldSeatIds.add(journeySeat.getSeat().getSeatId());
        }

        log.info("Held {} seats on schedule {} until {}", heldSeatIds.size(), scheduleId, expiry);
        return new HoldSeatsResponse(
                scheduleId,
                heldSeatIds,
                expiry,
                "Seats held successfully for " + holdDurationMinutes + " minutes"
        );
    }

    /**
     * Confirms booking after successful payment.
     */
    @Transactional
    public ConfirmSeatsResponse confirmBooking(Long scheduleId, ConfirmSeatsRequest request) {
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new ResourceNotFoundException("Schedule", "id", scheduleId);
        }

        LocalDateTime now = LocalDateTime.now();
        List<Long> bookedSeatIds = new ArrayList<>();

        for (Long seatId : request.seatIds()) {
            JourneySeat journeySeat = journeySeatRepository.findByScheduleIdAndSeatIdWithLock(scheduleId, seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat", "seatId", seatId));

            // Must be HELD by this reservation or AVAILABLE
            if (journeySeat.getStatus() == SeatStatus.BOOKED) {
                throw new SeatUnavailableException("Seat " + journeySeat.getSeat().getSeatNumber() +
                        " is already booked");
            }

            if (journeySeat.getStatus() == SeatStatus.HELD) {
                boolean holdExpired = journeySeat.getHoldExpiry() != null && journeySeat.getHoldExpiry().isBefore(now);
                boolean wrongReservation = journeySeat.getReservationId() != null &&
                        !journeySeat.getReservationId().equals(request.reservationId());

                if (holdExpired || wrongReservation) {
                    throw new SeatUnavailableException("Hold expired or belongs to another reservation for seat " +
                            journeySeat.getSeat().getSeatNumber());
                }
            }

            journeySeat.setStatus(SeatStatus.BOOKED);
            journeySeat.setHoldExpiry(null);
            journeySeat.setReservationId(request.reservationId());
            bookedSeatIds.add(journeySeat.getSeat().getSeatId());
        }

        log.info("Confirmed {} seats on schedule {} for reservation {}", bookedSeatIds.size(), scheduleId, request.reservationId());
        return new ConfirmSeatsResponse(
                scheduleId,
                bookedSeatIds,
                request.reservationId(),
                "Seats successfully booked"
        );
    }

    /**
     * Releases seats back to AVAILABLE on cancellation or payment failure.
     */
    @Transactional
    public void releaseSeats(Long scheduleId, ReleaseSeatsRequest request) {
        for (Long seatId : request.seatIds()) {
            journeySeatRepository.findByScheduleIdAndSeatIdWithLock(scheduleId, seatId).ifPresent(js -> {
                js.setStatus(SeatStatus.AVAILABLE);
                js.setHoldExpiry(null);
                js.setReservationId(null);
            });
        }
        log.info("Released {} seats on schedule {}", request.seatIds().size(), scheduleId);
    }

    /**
     * Returns all seat assignments for a given reservation (used by Reservation Service).
     */
    @Transactional(readOnly = true)
    public List<JourneySeatResponse> getJourneySeatsByReservation(Long reservationId) {
        return journeySeatRepository.findByReservationId(reservationId)
                .stream()
                .map(JourneySeatResponse::from)
                .toList();
    }


    /**
     * Periodic cleanup for expired holds.
     */
    @Transactional
    public int releaseExpiredHolds() {
        int releasedCount = journeySeatRepository.releaseExpiredHolds(LocalDateTime.now());
        if (releasedCount > 0) {
            log.info("Auto-released {} expired held seats", releasedCount);
        }
        return releasedCount;
    }
}
