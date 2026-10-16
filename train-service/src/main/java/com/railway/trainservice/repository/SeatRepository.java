package com.railway.trainservice.repository;

import com.railway.trainservice.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByCoachCoachId(Long coachId);
    List<Seat> findByCoachTrainTrainId(Long trainId);
}
