package com.meridian.lending.service.accrual;

import com.meridian.lending.domain.Loan;
import com.meridian.lending.repository.LoanRepository;
import com.meridian.lending.rules.InterestCalculator;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

/** Accrual in Java: one day's interest per active loan, using InterestCalculator (Actual/365). */
@Component
public class JavaAccrualJob implements AccrualEngine {
    private final LoanRepository loans;
    private final InterestCalculator interest;

    public JavaAccrualJob(LoanRepository loans, InterestCalculator interest) {
        this.loans = loans;
        this.interest = interest;
    }

    @Override
    public void accrue(LocalDate businessDate) {
        for (Loan loan : loans.findActive()) {
            loans.addAccruedInterest(loan.getLoanId(),
                    interest.interest(loan.getOutstanding(), loan.getAnnualRate(), 1));
        }
    }
}
