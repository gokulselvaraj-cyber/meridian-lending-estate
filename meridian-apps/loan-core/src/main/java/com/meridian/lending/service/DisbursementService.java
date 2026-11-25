package com.meridian.lending.service;

import com.meridian.lending.domain.Loan;
import com.meridian.lending.domain.LoanApplication;
import com.meridian.lending.domain.LoanStatus;
import com.meridian.lending.integration.LoanEventPublisher;
import com.meridian.lending.pricing.BrokerCommission;
import com.meridian.lending.pricing.ProductPlan;
import com.meridian.lending.repository.ApplicationRepository;
import com.meridian.lending.repository.CustomerRepository;
import com.meridian.lending.repository.LoanRepository;
import com.meridian.lending.rules.RateEngine;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** Turns an approved application into a loan, pays the money out and tells the rest of the bank. */
@Service
public class DisbursementService {
    private static final Logger log = LoggerFactory.getLogger(DisbursementService.class);
    private final RateEngine rates;
    private final ApplicationRepository applications;
    private final CustomerRepository customers;
    private final LoanRepository loans;
    private final RepaymentScheduleService schedules;
    private final BrokerCommission commission;
    private final LoanEventPublisher events;

    public DisbursementService(RateEngine rates, ApplicationRepository applications, CustomerRepository customers,
                               LoanRepository loans, RepaymentScheduleService schedules,
                               BrokerCommission commission, LoanEventPublisher events) {
        this.rates = rates;
        this.applications = applications;
        this.customers = customers;
        this.loans = loans;
        this.schedules = schedules;
        this.commission = commission;
        this.events = events;
    }

    /** Every 15 minutes, pays out applications that underwriting approved. */
    @Scheduled(fixedDelay = 900_000)
    public void disburseApproved() {
        for (LoanApplication a : applications.findApproved()) {
            disburse(a, ProductPlan.STANDARD, false);
            applications.markDisbursed(a.getApplicationId());
        }
    }

    /** @return the broker's commission, or zero when no broker introduced the loan */
    public BigDecimal disburse(LoanApplication approved, ProductPlan plan, boolean viaBroker) {
        if (approved.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException("only an approved application is disbursed");
        }
        // The rate is re-priced and locked on the day of disbursement.
        BigDecimal lockedRate = rates.price(approved.getCreditScore(), approved.ltv());
        Loan loan = new Loan();
        loan.setLoanId("L" + approved.getApplicationId().substring(0, 8));
        loan.setCustomerId(approved.getCustomerId());
        loan.setPrincipal(approved.getRequestedAmount());
        loan.setOutstanding(approved.getRequestedAmount());
        loan.setAnnualRate(lockedRate);
        loan.setLtvRatio(approved.ltv());
        loan.setPlan(plan);
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDisbursedOn(LocalDate.now());
        loans.insert(loan);
        schedules.create(loan, approved.getTermMonths());
        events.loanDisbursed(loan.getLoanId(), loan.getCustomerId(), loan.getPrincipal(), Instant.now());
        customers.findById(loan.getCustomerId())
                .ifPresent(c -> log.info("disbursed {} to {}", loan.getLoanId(), c));  // Customer.toString masks the id
        return viaBroker ? commission.commission(loan.getPrincipal()) : BigDecimal.ZERO;
    }
}
