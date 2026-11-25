package com.meridian.lending.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.meridian.lending.domain.LoanApplication;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class EligibilityPolicyTest {
    private final EligibilityPolicy policy = new EligibilityPolicy();

    private LoanApplication app(int score, String amount, String value) {
        LoanApplication a = new LoanApplication();
        a.setCreditScore(score);
        a.setRequestedAmount(new BigDecimal(amount));
        a.setPropertyValue(new BigDecimal(value));
        return a;
    }

    @Test
    void eligibleAtTheLimits() {
        assertTrue(policy.check(app(660, "80000", "100000")).isEmpty());
    }

    @Test
    void lowScoreAndHighLtvBothDecline() {
        assertEquals(2, policy.check(app(650, "82000", "100000")).size());
    }
}
