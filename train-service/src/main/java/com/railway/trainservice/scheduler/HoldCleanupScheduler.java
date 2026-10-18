package com.railway.trainservice.scheduler;

import com.railway.trainservice.service.SeatBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Background scheduler that periodically releases seat holds whose holdExpiry
 * timestamp has passed.
 *
 * Runs every 60 seconds (60,000 ms) with an initial delay of 10 seconds.
 */
@Component
@Slf4j
public class HoldCleanupScheduler {

    private final SeatBookingService seatBookingService;

    public HoldCleanupScheduler(SeatBookingService seatBookingService) {
        this.seatBookingService = seatBookingService;
    }

    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void cleanupExpiredHolds() {
        try {
            int released = seatBookingService.releaseExpiredHolds();
            if (released > 0) {
                log.info("HoldCleanupScheduler: Automatically released {} expired seat holds", released);
            }
        } catch (Exception e) {
            log.error("Error occurred while cleaning up expired seat holds: {}", e.getMessage());
        }
    }
}
