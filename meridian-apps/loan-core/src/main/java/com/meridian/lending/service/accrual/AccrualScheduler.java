package com.meridian.lending.service.accrual;

import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Starts the nightly accrual at 01:00 with whichever engine AccrualConfig chose. */
@Component
public class AccrualScheduler {
    private final AccrualEngine engine;

    public AccrualScheduler(AccrualEngine engine) {
        this.engine = engine;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void nightly() {
        engine.accrue(LocalDate.now());
    }
}
