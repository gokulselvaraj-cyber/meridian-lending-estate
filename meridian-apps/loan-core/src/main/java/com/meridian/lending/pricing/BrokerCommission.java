package com.meridian.lending.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

/** What a broker earns when a loan they introduced is disbursed. */
@Component
public class BrokerCommission {
    static final BigDecimal THRESHOLD = new BigDecimal("250000");
    static final BigDecimal RATE_BELOW = new BigDecimal("0.0050");    // 0.50% below 250,000
    static final BigDecimal RATE_AT_OR_ABOVE = new BigDecimal("0.0040"); // 0.40% at 250,000 or more

    public BigDecimal commission(BigDecimal loanAmount) {
        BigDecimal rate = loanAmount.compareTo(THRESHOLD) < 0 ? RATE_BELOW : RATE_AT_OR_ABOVE;
        return loanAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
