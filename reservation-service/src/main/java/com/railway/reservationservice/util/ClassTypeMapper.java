package com.railway.reservationservice.util;

import java.util.Map;

/**
 * Bidirectional mapper between reservation-service class type inputs
 * (SL, 3A, 2A, 1A) and train-service CoachType enum values (SL, AC3, AC2, AC1).
 *
 * This mapping is needed because:
 * - Client-facing API uses IRCTC-standard abbreviations (3A, 2A, 1A)
 * - train-service uses its internal CoachType enum names (AC3, AC2, AC1)
 */
public final class ClassTypeMapper {

    private ClassTypeMapper() {}

    private static final Map<String, String> CLIENT_TO_TRAIN = Map.of(
            "SL", "SL",
            "3A", "AC3",
            "2A", "AC2",
            "1A", "AC1"
    );

    private static final Map<String, String> TRAIN_TO_CLIENT = Map.of(
            "SL", "SL",
            "AC3", "3A",
            "AC2", "2A",
            "AC1", "1A"
    );

    /**
     * Converts client input (e.g. "3A") to train-service CoachType name (e.g. "AC3").
     * @throws IllegalArgumentException if the class type is not supported
     */
    public static String toTrainServiceClass(String clientClass) {
        String result = CLIENT_TO_TRAIN.get(clientClass.toUpperCase());
        if (result == null) {
            throw new IllegalArgumentException("Unsupported class type: " + clientClass +
                    ". Supported: SL, 3A, 2A, 1A");
        }
        return result;
    }

    /**
     * Converts train-service CoachType (e.g. "AC3") back to client abbreviation (e.g. "3A").
     */
    public static String toClientClass(String trainServiceClass) {
        return TRAIN_TO_CLIENT.getOrDefault(trainServiceClass, trainServiceClass);
    }
}
