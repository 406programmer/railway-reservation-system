package com.railway.trainservice.service;

import com.railway.trainservice.dto.*;
import com.railway.trainservice.entity.*;
import com.railway.trainservice.entity.enums.CoachType;
import com.railway.trainservice.entity.enums.SeatStatus;
import com.railway.trainservice.exception.InvalidOperationException;
import com.railway.trainservice.exception.ResourceNotFoundException;
import com.railway.trainservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final TrainRepository trainRepository;
    private final TrainScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final JourneySeatRepository journeySeatRepository;
    private final ClassFareRepository classFareRepository;

    @Transactional
    public ScheduleResponse createSchedule(CreateScheduleRequest request) {
        Train train = trainRepository.findById(request.trainId())
                .orElseThrow(() -> new ResourceNotFoundException("Train", "id", request.trainId()));

        if (scheduleRepository.existsByTrainTrainIdAndJourneyDate(request.trainId(), request.journeyDate())) {
            throw new InvalidOperationException("Schedule already exists for train " +
                    train.getTrainNumber() + " on " + request.journeyDate());
        }

        TrainSchedule schedule = TrainSchedule.builder()
                .train(train)
                .journeyDate(request.journeyDate())
                .departureTime(request.departureTime())
                .arrivalTime(request.arrivalTime())
                .platformNumber(request.platformNumber())
                .build();

        TrainSchedule savedSchedule = scheduleRepository.save(schedule);

        // Auto-populate journey_seats for all seats on this train
        List<Seat> seats = seatRepository.findByCoachTrainTrainId(train.getTrainId());
        if (seats.isEmpty()) {
            log.warn("Train {} has no coaches/seats yet. Schedule created with 0 initial journey seats.", train.getTrainNumber());
        } else {
            List<JourneySeat> journeySeats = seats.stream()
                    .map(seat -> JourneySeat.builder()
                            .schedule(savedSchedule)
                            .seat(seat)
                            .status(SeatStatus.AVAILABLE)
                            .build())
                    .toList();
            journeySeatRepository.saveAll(journeySeats);
            log.info("Initialized {} journey seats for schedule id {}", journeySeats.size(), savedSchedule.getScheduleId());
        }

        return ScheduleResponse.from(savedSchedule);
    }

    @Transactional(readOnly = true)
    public ScheduleResponse getScheduleById(Long scheduleId) {
        TrainSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", scheduleId));
        return ScheduleResponse.from(schedule);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedulesByTrain(Long trainId) {
        if (!trainRepository.existsById(trainId)) {
            throw new ResourceNotFoundException("Train", "id", trainId);
        }
        return scheduleRepository.findByTrainTrainId(trainId).stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    @Transactional
    public ScheduleResponse updateSchedule(Long scheduleId, UpdateScheduleRequest request) {
        TrainSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", scheduleId));

        if (request.status() != null) {
            schedule.setStatus(request.status());
        }
        if (request.delayMinutes() != null) {
            schedule.setDelayMinutes(request.delayMinutes());
        }
        if (request.platformNumber() != null) {
            schedule.setPlatformNumber(request.platformNumber());
        }

        TrainSchedule updated = scheduleRepository.save(schedule);
        log.info("Updated schedule id {}: status={}, delay={}m", scheduleId, updated.getStatus(), updated.getDelayMinutes());
        return ScheduleResponse.from(updated);
    }

    @Transactional(readOnly = true)
    public List<TrainSearchResponse> searchTrains(String source, String destination, LocalDate journeyDate) {
        List<TrainSchedule> schedules = scheduleRepository.searchSchedules(source.trim(), destination.trim(), journeyDate);

        return schedules.stream().map(schedule -> {
            Train train = schedule.getTrain();

            // Fetch fares for each class
            Map<CoachType, ClassFare> fareMap = classFareRepository.findByTrainTrainId(train.getTrainId())
                    .stream()
                    .collect(Collectors.toMap(ClassFare::getClassType, f -> f, (f1, f2) -> f1));

            // Calculate total and available seats per class on this schedule
            List<JourneySeat> allJourneySeats = journeySeatRepository.findByScheduleScheduleId(schedule.getScheduleId());
            Map<CoachType, List<JourneySeat>> seatsByClass = allJourneySeats.stream()
                    .collect(Collectors.groupingBy(js -> js.getSeat().getCoach().getCoachType()));

            List<ClassAvailabilityResponse> classAvailabilities = new ArrayList<>();
            for (Map.Entry<CoachType, List<JourneySeat>> entry : seatsByClass.entrySet()) {
                CoachType coachType = entry.getKey();
                List<JourneySeat> classSeats = entry.getValue();
                long total = classSeats.size();
                long available = classSeats.stream()
                        .filter(js -> js.getStatus() == SeatStatus.AVAILABLE)
                        .count();

                ClassFare fare = fareMap.get(coachType);
                BigDecimal fareAmount = (fare != null)
                        ? fare.calculateTotalFare(train.getDistanceKm())
                        : BigDecimal.ZERO;

                classAvailabilities.add(new ClassAvailabilityResponse(
                        coachType,
                        total,
                        available,
                        fareAmount
                ));
            }

            return new TrainSearchResponse(
                    schedule.getScheduleId(),
                    train.getTrainId(),
                    train.getTrainNumber(),
                    train.getTrainName(),
                    train.getTrainType(),
                    train.getSourceStation(),
                    train.getDestinationStation(),
                    train.getDistanceKm(),
                    schedule.getJourneyDate(),
                    schedule.getDepartureTime(),
                    schedule.getArrivalTime(),
                    schedule.getStatus(),
                    schedule.getDelayMinutes(),
                    schedule.getPlatformNumber(),
                    classAvailabilities
            );
        }).toList();
    }
}
