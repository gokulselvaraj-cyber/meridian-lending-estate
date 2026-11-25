package com.meridian.lending.rules;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

/** Interest for a period, day count Actual/365: the actual days elapsed over a 365-day year. */
@Component
public class InterestCalculator {
    static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");

    /** @param annualRatePercent e.g. 7.75 for 7.75% */
    public BigDecimal interest(BigDecimal principal, BigDecimal annualRatePercent, int actualDays) {
        return principal
                .multiply(annualRatePercent).divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(actualDays))
                .divide(DAYS_IN_YEAR, 2, RoundingMode.HALF_UP);
    }
}
