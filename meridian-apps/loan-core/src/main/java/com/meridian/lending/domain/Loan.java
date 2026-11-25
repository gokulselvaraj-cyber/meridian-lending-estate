package com.meridian.lending.domain;

import com.meridian.lending.pricing.ProductPlan;
import java.math.BigDecimal;
import java.time.LocalDate;

/** A disbursed loan. Stored in table LOAN; the mainframe sees the same row through copybook LOANREC. */
public class Loan {
    private String loanId;
    private String customerId;
    private BigDecimal principal;
    private BigDecimal outstanding;
    private BigDecimal annualRate;
    private BigDecimal accruedInterest;
    private BigDecimal ltvRatio;
    private ProductPlan plan = ProductPlan.STANDARD;
    private LoanStatus status;
    private LocalDate disbursedOn;

    public String getLoanId() { return loanId; }
    public void setLoanId(String loanId) { this.loanId = loanId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public BigDecimal getPrincipal() { return principal; }
    public void setPrincipal(BigDecimal principal) { this.principal = principal; }
    public BigDecimal getOutstanding() { return outstanding; }
    public void setOutstanding(BigDecimal outstanding) { this.outstanding = outstanding; }
    public BigDecimal getAnnualRate() { return annualRate; }
    public void setAnnualRate(BigDecimal annualRate) { this.annualRate = annualRate; }
    public BigDecimal getAccruedInterest() { return accruedInterest; }
    public void setAccruedInterest(BigDecimal accruedInterest) { this.accruedInterest = accruedInterest; }
    public BigDecimal getLtvRatio() { return ltvRatio; }
    public void setLtvRatio(BigDecimal ltvRatio) { this.ltvRatio = ltvRatio; }
    public ProductPlan getPlan() { return plan; }
    public void setPlan(ProductPlan plan) { this.plan = plan; }
    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }
    public LocalDate getDisbursedOn() { return disbursedOn; }
    public void setDisbursedOn(LocalDate disbursedOn) { this.disbursedOn = disbursedOn; }
}
