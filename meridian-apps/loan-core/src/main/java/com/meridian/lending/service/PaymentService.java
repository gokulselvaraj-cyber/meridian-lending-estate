package com.meridian.lending.service;

import com.meridian.lending.domain.Loan;
import com.meridian.lending.domain.Payment;
import com.meridian.lending.repository.LoanRepository;
import com.meridian.lending.repository.PaymentRepository;
import com.meridian.lending.servicing.LoanValidator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/** Records a payment and reduces the loan's outstanding balance. */
@Service
public class PaymentService {
    private final LoanRepository loans;
    private final PaymentRepository payments;
    private final LoanValidator validator;

    public PaymentService(LoanRepository loans, PaymentRepository payments,
                          @Qualifier("servicingLoanValidator") LoanValidator validator) {
        this.loans = loans;
        this.payments = payments;
        this.validator = validator;
    }

    public Payment record(String loanId, BigDecimal amount, String agent) {
        Loan loan = loans.findById(loanId).orElseThrow();
        validator.validatePayment(amount, loan.getOutstanding());
        Payment p = new Payment();
        p.setPaymentId(UUID.randomUUID().toString());
        p.setLoanId(loanId);
        p.setAmount(amount);
        p.setReceivedAt(Instant.now());
        p.setRecordedBy(agent);
        payments.save(p);
        loans.reduceOutstanding(loanId, amount);
        return p;
    }

    public Loan find(String loanId) {
        return loans.findById(loanId).orElseThrow();
    }
}
