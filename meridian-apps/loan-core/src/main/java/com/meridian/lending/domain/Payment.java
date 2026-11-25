package com.meridian.lending.domain;

import java.math.BigDecimal;
import java.time.Instant;

/** Money received against a loan. Stored in table PAYMENT. */
public class Payment {
    private String paymentId;
    private String loanId;
    private BigDecimal amount;
    private Instant receivedAt;
    private String recordedBy;

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getLoanId() { return loanId; }
    public void setLoanId(String loanId) { this.loanId = loanId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
}
