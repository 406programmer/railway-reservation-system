package com.railway.reservationservice.service;

import com.railway.reservationservice.client.PaymentClient;
import com.railway.reservationservice.client.TrainClient;
import com.railway.reservationservice.client.UserClient;
import com.railway.reservationservice.client.dto.*;
import com.railway.reservationservice.dto.*;
import com.railway.reservationservice.entity.Reservation;
import com.railway.reservationservice.entity.ReservationPassenger;
import com.railway.reservationservice.entity.ReservationSeat;
import com.railway.reservationservice.entity.enums.BookingStatus;
import com.railway.reservationservice.entity.enums.PassengerStatus;
import com.railway.reservationservice.exception.AccessDeniedException;
import com.railway.reservationservice.exception.InvalidCancellationException;
import com.railway.reservationservice.exception.ReservationNotFoundException;
import com.railway.reservationservice.exception.SeatAllocationException;
import com.railway.reservationservice.repository.ReservationRepository;
import com.railway.reservationservice.util.ClassTypeMapper;
import com.railway.reservationservice.util.PnrGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Core business service for Railway Reservations.
 *
 * GDPR CHECKLIST for Reservation Lifecycle
 * [x] Data minimisation applied — only required fields collected/returned
 * [x] Legal basis identified and stored (contractual performance)
 * [x] PII excluded from all log statements (uses only surrogate IDs: reservationId, scheduleId, userId)
 * [x] PII excluded from client-side storage
 * [x] Endpoint requires authentication
 * [x] Endpoint requires authorisation (ownership check enforced)
 * [x] Retention period documented
 * [x] Response serialisers used (DTO mapping)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TrainClient trainClient;
    private final UserClient userClient;
    private final PaymentClient paymentClient;
    private final PnrGenerator pnrGenerator;

    /**
     * Create a new reservation and hold seats.
     */
    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request, String userEmail) {
        log.info("Initiating reservation creation for schedule_id: {}", request.scheduleId());

        // 1. Resolve user ID from JWT subject email
        UserProfileDto userProfile = userClient.getUserByEmail(userEmail);
        if (userProfile == null || userProfile.id() == null) {
            throw new AccessDeniedException("Authenticated user profile could not be resolved");
        }
        Long userId = userProfile.id();

        // 2. Validate schedule
        TrainScheduleDto schedule = trainClient.getSchedule(request.scheduleId());
        if (schedule == null) {
            throw new ReservationNotFoundException("Schedule not found with id: " + request.scheduleId());
        }

        // 3. Map class and check availability & fare
        String trainCoachType = ClassTypeMapper.toTrainServiceClass(request.classType());
        ClassAvailabilityDto availability = trainClient.getAvailability(request.scheduleId(), trainCoachType);
        if (availability == null) {
            throw new SeatAllocationException("Could not retrieve availability for class: " + request.classType());
        }

        int requestedCount = request.passengers().size();
        if (availability.availableSeats() < requestedCount) {
            throw new SeatAllocationException(String.format(
                    "Insufficient seats available. Requested: %d, Available: %d",
                    requestedCount, availability.availableSeats()));
        }

        BigDecimal perSeatFare = availability.fare();
        BigDecimal totalFare = perSeatFare.multiply(BigDecimal.valueOf(requestedCount));

        // 4. Retrieve available seat IDs
        List<JourneySeatDto> availableSeats = trainClient.getSeats(request.scheduleId(), trainCoachType, "AVAILABLE");
        if (availableSeats == null || availableSeats.size() < requestedCount) {
            throw new SeatAllocationException("Unable to acquire specific seats for booking");
        }

        List<Long> selectedSeatIds = availableSeats.stream()
                .limit(requestedCount)
                .map(JourneySeatDto::seatId)
                .toList();

        // 5. Construct Reservation entity
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setScheduleId(request.scheduleId());
        reservation.setPassengerCount(requestedCount);
        reservation.setTotalFare(totalFare);
        reservation.setBookingStatus(BookingStatus.PENDING_PAYMENT);
        reservation.setHoldExpiresAt(LocalDateTime.now().plusMinutes(5));

        List<ReservationPassenger> passengers = new ArrayList<>();
        for (PassengerDetailDto dto : request.passengers()) {
            ReservationPassenger passenger = new ReservationPassenger();
            passenger.setReservation(reservation);
            passenger.setPassengerName(dto.name());
            passenger.setAge(dto.age());
            passenger.setGender(dto.gender());
            passenger.setFareAmount(perSeatFare);
            passenger.setPassengerStatus(PassengerStatus.PENDING_PAYMENT);
            passengers.add(passenger);
        }
        reservation.setPassengers(passengers);

        Reservation savedReservation = reservationRepository.save(reservation);

        // 6. Request Train-Service to hold seats
        try {
            HoldSeatsResponseDto holdResponse = trainClient.holdSeats(
                    request.scheduleId(),
                    new HoldSeatsRequestDto(selectedSeatIds, savedReservation.getReservationId())
            );
            if (holdResponse != null && holdResponse.holdExpiry() != null) {
                savedReservation.setHoldExpiresAt(holdResponse.holdExpiry());
                savedReservation = reservationRepository.save(savedReservation);
            }
            log.info("Seats held successfully for reservation_id: {}", savedReservation.getReservationId());
        } catch (Exception e) {
            log.error("Failed to hold seats for reservation_id: {}", savedReservation.getReservationId(), e);
            savedReservation.setBookingStatus(BookingStatus.PAYMENT_FAILED);
            reservationRepository.save(savedReservation);
            throw new SeatAllocationException("Failed to hold seats in train-service: " + e.getMessage());
        }

        return mapToReservationResponse(savedReservation);
    }

    /**
     * Handle payment notification callback from payment-service.
     */
    @Transactional
    public ReservationResponse handlePaymentCallback(Long reservationId, PaymentCallbackRequest request) {
        log.info("Processing payment callback for reservation_id: {}, status: {}", reservationId, request.status());

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + reservationId));

        // Idempotency: If already confirmed
        if (reservation.getBookingStatus() == BookingStatus.CONFIRMED && "SUCCESS".equalsIgnoreCase(request.status())) {
            return mapToReservationResponse(reservation);
        }

        if ("SUCCESS".equalsIgnoreCase(request.status())) {
            // Confirm seats in train-service
            List<JourneySeatDto> assignedSeats = trainClient.getSeatAssignments(reservationId);
            List<Long> seatIds = (assignedSeats != null)
                    ? assignedSeats.stream().map(JourneySeatDto::seatId).toList()
                    : List.of();

            trainClient.confirmSeats(
                    reservationId,
                    new ConfirmSeatsRequestDto(reservation.getScheduleId(), seatIds, reservationId)
            );

            // Re-fetch final assigned seats with coach & seat numbers
            assignedSeats = trainClient.getSeatAssignments(reservationId);

            String pnr = pnrGenerator.generateUniquePnr();
            reservation.setPnr(pnr);
            reservation.setPaymentId(request.paymentId());
            reservation.setBookingStatus(BookingStatus.CONFIRMED);

            List<ReservationPassenger> passengers = reservation.getPassengers();
            for (int i = 0; i < passengers.size(); i++) {
                ReservationPassenger passenger = passengers.get(i);
                passenger.setPassengerStatus(PassengerStatus.CONFIRMED);

                if (assignedSeats != null && i < assignedSeats.size()) {
                    JourneySeatDto seatDto = assignedSeats.get(i);
                    ReservationSeat seat = new ReservationSeat();
                    seat.setPassenger(passenger);
                    seat.setJourneySeatId(seatDto.journeySeatId());
                    seat.setCoachNumber(seatDto.coachNumber());
                    seat.setSeatNumber(seatDto.seatNumber());
                    seat.setClassType(seatDto.coachType());
                    seat.setAssignedAt(LocalDateTime.now());
                    passenger.setSeat(seat);
                }
            }

            Reservation updated = reservationRepository.save(reservation);
            log.info("Reservation confirmed successfully for reservation_id: {}", reservationId);
            return mapToReservationResponse(updated);
        } else {
            // Payment failed: release held seats
            List<JourneySeatDto> assignedSeats = trainClient.getSeatAssignments(reservationId);
            List<Long> seatIds = (assignedSeats != null)
                    ? assignedSeats.stream().map(JourneySeatDto::seatId).toList()
                    : List.of();

            try {
                trainClient.releaseSeats(
                        reservationId,
                        new ReleaseSeatsRequestDto(reservation.getScheduleId(), seatIds, reservationId)
                );
            } catch (Exception ex) {
                log.warn("Failed to release seats on payment failure for reservation_id: {}", reservationId, ex);
            }

            reservation.setBookingStatus(BookingStatus.PAYMENT_FAILED);
            for (ReservationPassenger passenger : reservation.getPassengers()) {
                passenger.setPassengerStatus(PassengerStatus.PAYMENT_FAILED);
            }

            Reservation updated = reservationRepository.save(reservation);
            log.info("Reservation marked as PAYMENT_FAILED for reservation_id: {}", reservationId);
            return mapToReservationResponse(updated);
        }
    }

    /**
     * Cancel an existing reservation.
     */
    @Transactional
    public CancellationResponse cancelReservation(Long reservationId, String userEmail, boolean isAdmin) {
        log.info("Cancellation requested for reservation_id: {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + reservationId));

        if (!isAdmin) {
            UserProfileDto userProfile = userClient.getUserByEmail(userEmail);
            if (userProfile == null || !reservation.getUserId().equals(userProfile.id())) {
                throw new AccessDeniedException("You do not have permission to cancel this reservation");
            }
        }

        if (reservation.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new InvalidCancellationException("Reservation is already cancelled");
        }
        if (reservation.getBookingStatus() == BookingStatus.EXPIRED || reservation.getBookingStatus() == BookingStatus.PAYMENT_FAILED) {
            throw new InvalidCancellationException("Cannot cancel a reservation with status: " + reservation.getBookingStatus());
        }

        // Release seats back to train-service
        List<JourneySeatDto> assignedSeats = trainClient.getSeatAssignments(reservationId);
        List<Long> seatIds = (assignedSeats != null)
                ? assignedSeats.stream().map(JourneySeatDto::seatId).toList()
                : List.of();

        try {
            trainClient.releaseSeats(
                    reservationId,
                    new ReleaseSeatsRequestDto(reservation.getScheduleId(), seatIds, reservationId)
            );
        } catch (Exception ex) {
            log.warn("Failed to release seats during cancellation for reservation_id: {}", reservationId, ex);
        }

        // Refund payment if previously confirmed
        if (reservation.getBookingStatus() == BookingStatus.CONFIRMED) {
            try {
                paymentClient.initiateRefund(new RefundRequestDto(
                        reservationId,
                        reservation.getPaymentId(),
                        reservation.getTotalFare(),
                        "Customer initiated cancellation"
                ));
            } catch (Exception ex) {
                log.error("Refund dispatch failed for reservation_id: {}", reservationId, ex);
            }
        }

        reservation.setBookingStatus(BookingStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());
        for (ReservationPassenger passenger : reservation.getPassengers()) {
            passenger.setPassengerStatus(PassengerStatus.CANCELLED);
        }

        Reservation updated = reservationRepository.save(reservation);
        log.info("Reservation cancelled successfully for reservation_id: {}", reservationId);

        return new CancellationResponse(
                updated.getReservationId(),
                updated.getPnr(),
                updated.getBookingStatus().name(),
                "Reservation successfully cancelled",
                updated.getCancelledAt()
        );
    }

    /**
     * Get reservation by ID with ownership enforcement.
     */
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long reservationId, String userEmail, boolean isAdmin) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + reservationId));

        if (!isAdmin) {
            UserProfileDto userProfile = userClient.getUserByEmail(userEmail);
            if (userProfile == null || !reservation.getUserId().equals(userProfile.id())) {
                throw new AccessDeniedException("You do not have permission to view this reservation");
            }
        }

        return mapToReservationResponse(reservation);
    }

    /**
     * Get reservation by PNR.
     */
    @Transactional(readOnly = true)
    public ReservationResponse getReservationByPnr(String pnr, String userEmail, boolean isAdmin) {
        Reservation reservation = reservationRepository.findByPnr(pnr)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found for PNR: " + pnr));

        if (!isAdmin) {
            UserProfileDto userProfile = userClient.getUserByEmail(userEmail);
            if (userProfile == null || !reservation.getUserId().equals(userProfile.id())) {
                throw new AccessDeniedException("You do not have permission to view this reservation");
            }
        }

        return mapToReservationResponse(reservation);
    }

    /**
     * List all reservations for the authenticated user.
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> getUserReservations(String userEmail) {
        UserProfileDto userProfile = userClient.getUserByEmail(userEmail);
        if (userProfile == null || userProfile.id() == null) {
            throw new AccessDeniedException("Authenticated user profile could not be resolved");
        }

        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userProfile.id())
                .stream()
                .map(this::mapToReservationResponse)
                .toList();
    }

    /**
     * Admin: Paginated reservation search with optional status filter.
     */
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getAllReservations(BookingStatus status, Pageable pageable) {
        return reservationRepository.findAllWithStatusFilter(status, pageable)
                .map(this::mapToReservationResponse);
    }

    /**
     * Scheduled job to release expired seat holds every 60 seconds.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredHolds() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredList = reservationRepository.findByBookingStatusAndHoldExpiresAtBefore(
                BookingStatus.PENDING_PAYMENT,
                now
        );

        for (Reservation reservation : expiredList) {
            log.info("Expiring stale reservation_id: {}", reservation.getReservationId());
            try {
                List<JourneySeatDto> assignedSeats = trainClient.getSeatAssignments(reservation.getReservationId());
                List<Long> seatIds = (assignedSeats != null)
                        ? assignedSeats.stream().map(JourneySeatDto::seatId).toList()
                        : List.of();

                trainClient.releaseSeats(
                        reservation.getReservationId(),
                        new ReleaseSeatsRequestDto(reservation.getScheduleId(), seatIds, reservation.getReservationId())
                );
            } catch (Exception ex) {
                log.warn("Failed to release seats for expired reservation_id: {}", reservation.getReservationId(), ex);
            }

            reservation.setBookingStatus(BookingStatus.EXPIRED);
            for (ReservationPassenger passenger : reservation.getPassengers()) {
                passenger.setPassengerStatus(PassengerStatus.EXPIRED);
            }
            reservationRepository.save(reservation);
        }
    }

    private ReservationResponse mapToReservationResponse(Reservation r) {
        List<PassengerResponse> passengerResponses = r.getPassengers().stream()
                .map(p -> PassengerResponse.from(p, SeatAssignmentResponse.from(p.getSeat())))
                .toList();
        return ReservationResponse.from(r, passengerResponses);
    }
}
