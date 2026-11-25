package com.meridian.lending.service;

import com.meridian.lending.repository.LoanRepository;
import com.meridian.lending.rules.RetentionPolicy;
import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** Each January, purges closed loans older than the retention period. */
@Service
public class RetentionJob {
    private final LoanRepository loans;

    public RetentionJob(LoanRepository loans) {
        this.loans = loans;
    }

    @Scheduled(cron = "0 0 2 2 1 *")
    public int purge() {
        return loans.purgeClosedBefore(LocalDate.now().minusYears(RetentionPolicy.RETENTION_YEARS));
    }
}
