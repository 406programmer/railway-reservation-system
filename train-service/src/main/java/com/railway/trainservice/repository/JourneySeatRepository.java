package com.railway.trainservice.repository;

import com.railway.trainservice.entity.JourneySeat;
import com.railway.trainservice.entity.enums.CoachType;
import com.railway.trainservice.entity.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JourneySeatRepository extends JpaRepository<JourneySeat, Long> {

    List<JourneySeat> findByScheduleScheduleId(Long scheduleId);

    List<JourneySeat> findByScheduleScheduleIdAndSeatCoachCoachType(Long scheduleId, CoachType coachType);

    List<JourneySeat> findByScheduleScheduleIdAndStatus(Long scheduleId, SeatStatus status);

    Optional<JourneySeat> findByScheduleScheduleIdAndSeatSeatId(Long scheduleId, Long seatId);

    long countByScheduleScheduleIdAndSeatCoachCoachTypeAndStatus(Long scheduleId, CoachType coachType, SeatStatus status);

    long countByScheduleScheduleIdAndSeatCoachCoachType(Long scheduleId, CoachType coachType);

    List<JourneySeat> findByReservationId(Long reservationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT js FROM JourneySeat js WHERE js.journeySeatId = :journeySeatId")
    Optional<JourneySeat> findByIdWithLock(@Param("journeySeatId") Long journeySeatId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT js FROM JourneySeat js WHERE js.schedule.scheduleId = :scheduleId AND js.seat.seatId = :seatId")
    Optional<JourneySeat> findByScheduleIdAndSeatIdWithLock(
            @Param("scheduleId") Long scheduleId,
            @Param("seatId") Long seatId
    );

    @Modifying
    @Query("UPDATE JourneySeat js " +
           "SET js.status = com.railway.trainservice.entity.enums.SeatStatus.AVAILABLE, " +
           "    js.holdExpiry = null, " +
           "    js.reservationId = null " +
           "WHERE js.status = com.railway.trainservice.entity.enums.SeatStatus.HELD " +
           "AND js.holdExpiry < :now")
    int releaseExpiredHolds(@Param("now") LocalDateTime now);
}
