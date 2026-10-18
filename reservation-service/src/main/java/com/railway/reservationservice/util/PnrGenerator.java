package com.railway.reservationservice.util;

import com.railway.reservationservice.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates unique 10-digit numeric Passenger Name Record (PNR) numbers.
 * Follows the Indian Railways convention of 10 numeric digits.
 * Retries up to 5 times if a collision is detected in the database.
 */
@Component
@RequiredArgsConstructor
public class PnrGenerator {

    private final ReservationRepository reservationRepository;

    /**
     * Generate a unique 10-digit numeric PNR.
     *
     * @return 10-digit numeric PNR string
     * @throws IllegalStateException if a unique PNR cannot be generated after max attempts
     */
    public String generateUniquePnr() {
        int maxAttempts = 5;
        for (int i = 0; i < maxAttempts; i++) {
            // Generate a 10-digit number between 1000000000L and 9999999999L
            long pnrNumber = ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L);
            String pnr = String.valueOf(pnrNumber);
            if (!reservationRepository.existsByPnr(pnr)) {
                return pnr;
            }
        }
        throw new IllegalStateException("Failed to generate a unique PNR after " + maxAttempts + " attempts");
    }
}
