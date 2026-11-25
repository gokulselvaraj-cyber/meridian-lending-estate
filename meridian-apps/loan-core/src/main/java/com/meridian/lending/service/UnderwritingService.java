package com.meridian.lending.service;

import com.meridian.lending.domain.LoanApplication;
import com.meridian.lending.domain.LoanStatus;
import com.meridian.lending.integration.CreditBureauClient;
import com.meridian.lending.rules.EligibilityPolicy;
import com.meridian.lending.rules.RateEngine;
import java.util.List;
import org.springframework.stereotype.Service;

/** Decides an application: bureau score, eligibility rules, then a price for the approved ones. */
@Service
public class UnderwritingService {
    private final CreditBureauClient bureau;
    private final EligibilityPolicy eligibility;
    private final RateEngine rates;

    public UnderwritingService(CreditBureauClient bureau, EligibilityPolicy eligibility, RateEngine rates) {
        this.bureau = bureau;
        this.eligibility = eligibility;
        this.rates = rates;
    }

    public void assess(LoanApplication application) {
        application.setCreditScore(bureau.score(application.getCustomerId()));
        List<String> reasons = eligibility.check(application);
        if (!reasons.isEmpty()) {
            application.setStatus(LoanStatus.DECLINED);
            application.setDeclineReason(String.join("; ", reasons));
            return;
        }
        application.setOfferedRate(rates.price(application.getCreditScore(), application.ltv()));
        application.setStatus(LoanStatus.APPROVED);
    }
}
