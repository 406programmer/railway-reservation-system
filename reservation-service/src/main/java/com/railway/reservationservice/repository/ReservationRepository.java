package com.railway.reservationservice.repository;

import com.railway.reservationservice.entity.Reservation;
import com.railway.reservationservice.entity.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // PRIVACY: Returns reservations filtered by userId. Auth required: yes. PII fields: none at this level.
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Reservation> findByPnr(String pnr);

    // Admin: paginated list with optional status filter
    @Query("SELECT r FROM Reservation r WHERE (:status IS NULL OR r.bookingStatus = :status)")
    Page<Reservation> findAllWithStatusFilter(@Param("status") BookingStatus status, Pageable pageable);

    boolean existsByPnr(String pnr);

    List<Reservation> findByBookingStatusAndHoldExpiresAtBefore(BookingStatus bookingStatus, java.time.LocalDateTime holdExpiresAt);
}
