package com.meridian.lending.controller;

import com.meridian.lending.domain.Loan;
import com.meridian.lending.domain.Payment;
import com.meridian.lending.service.PaymentService;
import java.math.BigDecimal;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {
    public record PaymentRequest(BigDecimal amount) {
    }

    private final PaymentService payments;

    public LoanController(PaymentService payments) {
        this.payments = payments;
    }

    @GetMapping("/{loanId}")
    public Loan get(@PathVariable("loanId") String loanId) {
        return payments.find(loanId);
    }

    @PostMapping("/{loanId}/payments")
    public Payment pay(@PathVariable("loanId") String loanId, @RequestBody PaymentRequest req, Principal agent) {
        return payments.record(loanId, req.amount(), agent.getName());
    }
}
