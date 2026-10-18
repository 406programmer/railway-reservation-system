package com.railway.reservationservice.repository;

import com.railway.reservationservice.entity.ReservationPassenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationPassengerRepository extends JpaRepository<ReservationPassenger, Long> {

    List<ReservationPassenger> findByReservation_ReservationId(Long reservationId);
}
