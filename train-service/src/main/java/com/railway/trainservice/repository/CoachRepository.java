package com.railway.trainservice.repository;

import com.railway.trainservice.entity.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoachRepository extends JpaRepository<Coach, Long> {
    List<Coach> findByTrainTrainId(Long trainId);
    Optional<Coach> findByTrainTrainIdAndCoachNumber(Long trainId, String coachNumber);
    boolean existsByTrainTrainIdAndCoachNumber(Long trainId, String coachNumber);
}
