package com.meridian.lending.rules;

import java.time.LocalDate;
import org.springframework.stereotype.Component;

/** How long a closed loan's records are kept before they are purged. */
@Component
public class RetentionPolicy {
    public static final int RETENTION_YEARS = 7;

    public boolean mayPurge(LocalDate closedOn, LocalDate today) {
        return closedOn.plusYears(RETENTION_YEARS).isBefore(today);
    }
}
