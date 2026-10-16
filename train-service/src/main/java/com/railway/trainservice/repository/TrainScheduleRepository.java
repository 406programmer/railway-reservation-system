package com.railway.trainservice.repository;

import com.railway.trainservice.entity.TrainSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainScheduleRepository extends JpaRepository<TrainSchedule, Long> {
    List<TrainSchedule> findByTrainTrainId(Long trainId);
    Optional<TrainSchedule> findByTrainTrainIdAndJourneyDate(Long trainId, LocalDate journeyDate);
    boolean existsByTrainTrainIdAndJourneyDate(Long trainId, LocalDate journeyDate);

    @Query("SELECT s FROM TrainSchedule s " +
           "JOIN FETCH s.train t " +
           "WHERE LOWER(t.sourceStation) = LOWER(:source) " +
           "AND LOWER(t.destinationStation) = LOWER(:destination) " +
           "AND s.journeyDate = :date " +
           "AND s.status != com.railway.trainservice.entity.enums.ScheduleStatus.CANCELLED")
    List<TrainSchedule> searchSchedules(
            @Param("source") String source,
            @Param("destination") String destination,
            @Param("date") LocalDate date
    );
}
