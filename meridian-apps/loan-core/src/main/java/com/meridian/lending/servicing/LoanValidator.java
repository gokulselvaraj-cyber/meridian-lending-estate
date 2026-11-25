package com.meridian.lending.servicing;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** Checks a payment against the loan it is paid into. */
@Component("servicingLoanValidator")
public class LoanValidator {
    public void validatePayment(BigDecimal amount, BigDecimal outstanding) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("payment must be more than zero");
        }
        if (amount.compareTo(outstanding) > 0) {
            throw new IllegalArgumentException("payment is more than the outstanding balance");
        }
    }
}
