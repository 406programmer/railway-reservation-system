package com.railway.trainservice.repository;

import com.railway.trainservice.entity.ClassFare;
import com.railway.trainservice.entity.enums.CoachType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassFareRepository extends JpaRepository<ClassFare, Long> {
    List<ClassFare> findByTrainTrainId(Long trainId);
    Optional<ClassFare> findByTrainTrainIdAndClassType(Long trainId, CoachType classType);
    boolean existsByTrainTrainIdAndClassType(Long trainId, CoachType classType);
}
