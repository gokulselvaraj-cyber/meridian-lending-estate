package com.meridian.lending.rules;

import com.meridian.lending.domain.LoanApplication;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** Who may borrow: the underwriting rules applied to every application. */
@Component
public class EligibilityPolicy {
    public static final int MIN_CREDIT_SCORE = 660;
    public static final BigDecimal MAX_LTV = new BigDecimal("0.80");

    /** The reasons the application fails; empty when it is eligible. */
    public List<String> check(LoanApplication application) {
        List<String> reasons = new ArrayList<>();
        if (application.getCreditScore() == null || application.getCreditScore() < MIN_CREDIT_SCORE) {
            reasons.add("credit score below " + MIN_CREDIT_SCORE);
        }
        if (application.ltv().compareTo(MAX_LTV) > 0) {
            reasons.add("loan-to-value above " + MAX_LTV);
        }
        return reasons;
    }
}
