package com.meridian.lending.rules;

import java.math.BigDecimal;

/** The flat late fee used before LateFeePolicy. */
public class LegacyFeeCalculator {
    static final BigDecimal FLAT_FEE = new BigDecimal("25.00");

    public BigDecimal fee(int daysLate) {
        return daysLate > 0 ? FLAT_FEE : BigDecimal.ZERO;
    }
}
