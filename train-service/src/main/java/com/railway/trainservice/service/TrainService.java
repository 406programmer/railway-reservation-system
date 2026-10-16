package com.railway.trainservice.service;

import com.railway.trainservice.dto.*;
import com.railway.trainservice.entity.*;
import com.railway.trainservice.entity.enums.BerthType;
import com.railway.trainservice.entity.enums.CoachType;
import com.railway.trainservice.exception.InvalidOperationException;
import com.railway.trainservice.exception.ResourceNotFoundException;
import com.railway.trainservice.repository.ClassFareRepository;
import com.railway.trainservice.repository.CoachRepository;
import com.railway.trainservice.repository.SeatRepository;
import com.railway.trainservice.repository.TrainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainService {

    private final TrainRepository trainRepository;
    private final CoachRepository coachRepository;
    private final SeatRepository seatRepository;
    private final ClassFareRepository classFareRepository;

    @Transactional
    public TrainResponse createTrain(CreateTrainRequest request) {
        if (trainRepository.existsByTrainNumber(request.trainNumber())) {
            throw new InvalidOperationException("Train with number " + request.trainNumber() + " already exists");
        }

        Train train = Train.builder()
                .trainNumber(request.trainNumber().trim().toUpperCase())
                .trainName(request.trainName().trim())
                .trainType(request.trainType())
                .sourceStation(request.sourceStation().trim().toUpperCase())
                .destinationStation(request.destinationStation().trim().toUpperCase())
                .distanceKm(request.distanceKm())
                .build();

        Train saved = trainRepository.save(train);
        log.info("Created train: {} - {}", saved.getTrainNumber(), saved.getTrainName());
        return TrainResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public TrainResponse getTrainById(Long trainId) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new ResourceNotFoundException("Train", "id", trainId));
        return TrainResponse.from(train);
    }

    @Transactional(readOnly = true)
    public TrainResponse getTrainByNumber(String trainNumber) {
        Train train = trainRepository.findByTrainNumber(trainNumber.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Train", "number", trainNumber));
        return TrainResponse.from(train);
    }

    @Transactional(readOnly = true)
    public List<TrainResponse> getAllTrains() {
        return trainRepository.findAll().stream()
                .map(TrainResponse::from)
                .toList();
    }

    @Transactional
    public void deleteTrain(Long trainId) {
        if (!trainRepository.existsById(trainId)) {
            throw new ResourceNotFoundException("Train", "id", trainId);
        }
        trainRepository.deleteById(trainId);
        log.info("Deleted train id: {}", trainId);
    }

    // ── Coaches & Automatic Seat Layout Generation ────────────────────────

    @Transactional
    public CoachResponse addCoach(Long trainId, AddCoachRequest request) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new ResourceNotFoundException("Train", "id", trainId));

        String coachNumber = request.coachNumber().trim().toUpperCase();
        if (coachRepository.existsByTrainTrainIdAndCoachNumber(trainId, coachNumber)) {
            throw new InvalidOperationException("Coach " + coachNumber + " already exists for train " + train.getTrainNumber());
        }

        int seatCount = determineDefaultSeatCount(request.coachType(), request.totalSeats());

        Coach coach = Coach.builder()
                .train(train)
                .coachNumber(coachNumber)
                .coachType(request.coachType())
                .totalSeats(seatCount)
                .build();

        Coach savedCoach = coachRepository.save(coach);

        // Auto-generate realistic Indian Railway seats layout
        List<Seat> seats = generateSeatsForCoach(savedCoach, seatCount, request.coachType());
        seatRepository.saveAll(seats);

        log.info("Added coach {} to train {} with {} seats", coachNumber, train.getTrainNumber(), seatCount);
        return CoachResponse.from(savedCoach);
    }

    @Transactional(readOnly = true)
    public List<CoachResponse> getCoachesByTrain(Long trainId) {
        if (!trainRepository.existsById(trainId)) {
            throw new ResourceNotFoundException("Train", "id", trainId);
        }
        return coachRepository.findByTrainTrainId(trainId).stream()
                .map(CoachResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsByCoach(Long coachId) {
        if (!coachRepository.existsById(coachId)) {
            throw new ResourceNotFoundException("Coach", "id", coachId);
        }
        return seatRepository.findByCoachCoachId(coachId).stream()
                .map(SeatResponse::from)
                .toList();
    }

    // ── Class Fares ───────────────────────────────────────────────────────

    @Transactional
    public ClassFareResponse configureFare(Long trainId, ClassFareRequest request) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new ResourceNotFoundException("Train", "id", trainId));

        BigDecimal baseFare = request.baseFare() != null ? request.baseFare() : BigDecimal.ZERO;

        ClassFare fare = classFareRepository.findByTrainTrainIdAndClassType(trainId, request.classType())
                .map(existing -> {
                    existing.setBaseFare(baseFare);
                    existing.setFarePerKm(request.farePerKm());
                    return existing;
                })
                .orElseGet(() -> ClassFare.builder()
                        .train(train)
                        .classType(request.classType())
                        .baseFare(baseFare)
                        .farePerKm(request.farePerKm())
                        .build());

        ClassFare saved = classFareRepository.save(fare);
        log.info("Configured fare for train {} class {}: base={}, perKm={}",
                train.getTrainNumber(), request.classType(), baseFare, request.farePerKm());
        return ClassFareResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ClassFareResponse> getFaresByTrain(Long trainId) {
        if (!trainRepository.existsById(trainId)) {
            throw new ResourceNotFoundException("Train", "id", trainId);
        }
        return classFareRepository.findByTrainTrainId(trainId).stream()
                .map(ClassFareResponse::from)
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private int determineDefaultSeatCount(CoachType coachType, Integer totalSeats) {
        if (totalSeats != null && totalSeats > 0) {
            return totalSeats;
        }
        return switch (coachType) {
            case SL -> 72;
            case AC3 -> 64;
            case AC2 -> 48;
            case AC1 -> 24;
            case CC -> 78;
            case EC -> 56;
            case GEN -> 100;
        };
    }

    private List<Seat> generateSeatsForCoach(Coach coach, int seatCount, CoachType coachType) {
        List<Seat> seats = new ArrayList<>(seatCount);
        for (int i = 1; i <= seatCount; i++) {
            BerthType berthType = calculateBerthType(i, coachType);
            seats.add(Seat.builder()
                    .coach(coach)
                    .seatNumber(String.valueOf(i))
                    .berthType(berthType)
                    .build());
        }
        return seats;
    }

    private BerthType calculateBerthType(int seatNum, CoachType coachType) {
        return switch (coachType) {
            case SL, AC3 -> {
                int mod = seatNum % 8;
                yield switch (mod) {
                    case 1, 4 -> BerthType.LOWER;
                    case 2, 5 -> BerthType.MIDDLE;
                    case 3, 6 -> BerthType.UPPER;
                    case 7 -> BerthType.SIDE_LOWER;
                    case 0 -> BerthType.SIDE_UPPER;
                    default -> BerthType.LOWER;
                };
            }
            case AC2 -> {
                int mod = seatNum % 6;
                yield switch (mod) {
                    case 1, 3 -> BerthType.LOWER;
                    case 2, 4 -> BerthType.UPPER;
                    case 5 -> BerthType.SIDE_LOWER;
                    case 0 -> BerthType.SIDE_UPPER;
                    default -> BerthType.LOWER;
                };
            }
            case AC1 -> (seatNum % 2 == 1) ? BerthType.LOWER : BerthType.UPPER;
            case CC, EC -> (seatNum % 3 == 1) ? BerthType.WINDOW : BerthType.AISLE;
            case GEN -> BerthType.WINDOW;
        };
    }
}
