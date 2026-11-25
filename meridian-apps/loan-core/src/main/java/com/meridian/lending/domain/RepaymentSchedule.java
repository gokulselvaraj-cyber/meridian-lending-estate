package com.meridian.lending.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** The installments a borrower owes. Stored in table REPAYMENT_SCHEDULE, one row per installment. */
public class RepaymentSchedule {
    public record Installment(int installmentNo, LocalDate dueDate, BigDecimal amountDue, boolean paid) {
    }

    private final String loanId;
    private final List<Installment> installments = new ArrayList<>();

    public RepaymentSchedule(String loanId) {
        this.loanId = loanId;
    }

    public String getLoanId() { return loanId; }
    public List<Installment> getInstallments() { return installments; }
    public void add(Installment i) { installments.add(i); }
}
