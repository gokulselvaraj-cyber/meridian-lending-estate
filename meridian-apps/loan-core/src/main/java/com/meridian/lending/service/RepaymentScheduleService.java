package com.meridian.lending.service;

import com.meridian.lending.domain.Loan;
import com.meridian.lending.domain.LoanStatus;
import com.meridian.lending.domain.RepaymentSchedule;
import com.meridian.lending.integration.LoanEventPublisher;
import com.meridian.lending.repository.LoanRepository;
import com.meridian.lending.repository.ScheduleRepository;
import com.meridian.lending.rules.LateFeePolicy;
import com.meridian.lending.rules.RateEngine;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Builds a loan's installment plan. Each morning it reports installments missed yesterday (PaymentMissed) and
 * charges the late fee on installments that have just passed the grace period.
 */
@Service
public class RepaymentScheduleService {
    private final RateEngine rates;
    private final ScheduleRepository schedules;
    private final LoanRepository loans;
    private final LoanEventPublisher events;
    private final LateFeePolicy lateFees;

    public RepaymentScheduleService(RateEngine rates, ScheduleRepository schedules, LoanRepository loans,
                                    LoanEventPublisher events, LateFeePolicy lateFees) {
        this.rates = rates;
        this.schedules = schedules;
        this.loans = loans;
        this.events = events;
        this.lateFees = lateFees;
    }

    public RepaymentSchedule create(Loan loan, int termMonths) {
        BigDecimal annual = loan.getAnnualRate() != null ? loan.getAnnualRate()
                : rates.price(700, loan.getLtvRatio());
        BigDecimal monthly = annual.divide(new BigDecimal("1200"), MathContext.DECIMAL64);
        BigDecimal factor = BigDecimal.ONE.add(monthly).pow(termMonths, MathContext.DECIMAL64);
        BigDecimal installment = loan.getPrincipal().multiply(monthly).multiply(factor)
                .divide(factor.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
        RepaymentSchedule s = new RepaymentSchedule(loan.getLoanId());
        for (int n = 1; n <= termMonths; n++) {
            s.add(new RepaymentSchedule.Installment(n, loan.getDisbursedOn().plusMonths(n), installment, false));
        }
        schedules.save(s);
        return s;
    }

    @Scheduled(cron = "0 30 6 * * *")
    public void reportMissed() {
        LocalDate today = LocalDate.now();
        for (RepaymentSchedule.Installment i : schedules.findOverdue(today)) {
            String loanId = schedules.loanIdOf(i.installmentNo(), i.dueDate());
            Loan loan = loans.findById(loanId).orElseThrow();
            int daysLate = (int) ChronoUnit.DAYS.between(i.dueDate(), today);
            if (daysLate == 1) {
                loans.setStatus(loanId, LoanStatus.DELINQUENT);
                events.paymentMissed(loanId, loan.getCustomerId(), i.installmentNo(), i.amountDue(), i.dueDate());
            }
            if (daysLate == LateFeePolicy.GRACE_DAYS + 1) {
                BigDecimal fee = lateFees.fee(i.amountDue(), daysLate, loan.getPlan(),
                        loans.lateFeesWaivedThisYear(loanId));
                if (fee.signum() > 0) {
                    loans.chargeLateFee(loanId, fee);
                } else {
                    loans.recordWaivedLateFee(loanId);
                }
            }
        }
    }
}
