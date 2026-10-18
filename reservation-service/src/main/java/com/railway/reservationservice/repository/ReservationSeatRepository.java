package com.railway.reservationservice.repository;

import com.railway.reservationservice.entity.ReservationSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {

    List<ReservationSeat> findByPassenger_Reservation_ReservationId(Long reservationId);
}
