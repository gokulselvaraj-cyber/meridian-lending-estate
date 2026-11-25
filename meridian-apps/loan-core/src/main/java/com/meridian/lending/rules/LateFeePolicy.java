package com.meridian.lending.rules;

import com.meridian.lending.pricing.ProductPlan;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

/** The fee charged when an installment is paid late. */
@Component
public class LateFeePolicy {
    public static final int GRACE_DAYS = 10;
    static final BigDecimal FEE_RATE = new BigDecimal("0.05");
    static final BigDecimal MINIMUM_FEE = new BigDecimal("15.00");

    /**
     * @param installment       the overdue installment amount
     * @param daysLate          days past the due date
     * @param plan              the borrower's product plan
     * @param feesWaivedThisYear late fees already waived for this borrower in the current calendar year
     */
    public BigDecimal fee(BigDecimal installment, int daysLate, ProductPlan plan, int feesWaivedThisYear) {
        if (daysLate <= GRACE_DAYS) {
            return BigDecimal.ZERO;
        }
        if (plan.waivesFirstLateFeeEachYear() && feesWaivedThisYear == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal fee = installment.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        return fee.max(MINIMUM_FEE);
    }
}
