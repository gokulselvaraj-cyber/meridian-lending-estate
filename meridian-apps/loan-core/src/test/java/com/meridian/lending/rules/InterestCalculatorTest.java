package com.meridian.lending.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class InterestCalculatorTest {
    private final InterestCalculator calc = new InterestCalculator();

    @Test
    void oneMonthOfInterestIsActualOver365() {
        assertEquals(new BigDecimal("658.22"), calc.interest(new BigDecimal("100000"), new BigDecimal("7.75"), 31));
    }

    @Test
    void zeroDaysIsZero() {
        assertEquals(new BigDecimal("0.00"), calc.interest(new BigDecimal("100000"), new BigDecimal("7.75"), 0));
    }
}
