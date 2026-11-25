package com.meridian.lending.service;

import com.meridian.lending.domain.Loan;
import com.meridian.lending.repository.LoanRepository;
import com.meridian.lending.rules.RateEngine;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** Each month, finds active loans that could be refinanced at a lower rate. */
@Service
public class RefinanceService {
    private final RateEngine rates;
    private final LoanRepository loans;
    private final BigDecimal maxLtv;

    public RefinanceService(RateEngine rates, LoanRepository loans, @Value("${lending.max-ltv}") BigDecimal maxLtv) {
        this.rates = rates;
        this.loans = loans;
        this.maxLtv = maxLtv;
    }

    @Scheduled(cron = "0 0 3 1 * *")
    public List<String> findOffers() {
        List<String> offers = new ArrayList<>();
        for (Loan loan : loans.findActive()) {
            if (loan.getLtvRatio().compareTo(maxLtv) > 0) {
                continue;
            }
            BigDecimal best = rates.price(760, loan.getLtvRatio());
            if (best.compareTo(loan.getAnnualRate()) < 0) {
                offers.add(loan.getLoanId());
            }
        }
        return offers;
    }
}
