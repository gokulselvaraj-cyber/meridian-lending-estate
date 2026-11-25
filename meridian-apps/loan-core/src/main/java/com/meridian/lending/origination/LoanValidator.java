package com.meridian.lending.origination;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** Checks a new application's requested amount. */
@Component("originationLoanValidator")
public class LoanValidator {
    public static final BigDecimal MIN_AMOUNT = new BigDecimal("5000");
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("2000000");

    public void validate(BigDecimal requestedAmount) {
        if (requestedAmount == null
                || requestedAmount.compareTo(MIN_AMOUNT) < 0
                || requestedAmount.compareTo(MAX_AMOUNT) > 0) {
            throw new IllegalArgumentException("requested amount must be between 5,000 and 2,000,000");
        }
    }
}
