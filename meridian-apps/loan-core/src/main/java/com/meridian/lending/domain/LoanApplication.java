package com.meridian.lending.domain;

import java.math.BigDecimal;
import java.time.Instant;

/** A request to borrow. Stored in table LOAN_APPLICATION. Becomes a Loan only when approved and disbursed. */
public class LoanApplication {
    private String applicationId;
    private String customerId;
    private BigDecimal requestedAmount;
    private BigDecimal propertyValue;
    private int termMonths;
    private Integer creditScore;
    private BigDecimal offeredRate;
    private LoanStatus status = LoanStatus.APPLIED;
    private String declineReason;
    private Instant submittedAt;

    public BigDecimal ltv() {
        return requestedAmount.divide(propertyValue, 4, java.math.RoundingMode.HALF_UP);
    }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }
    public BigDecimal getPropertyValue() { return propertyValue; }
    public void setPropertyValue(BigDecimal propertyValue) { this.propertyValue = propertyValue; }
    public int getTermMonths() { return termMonths; }
    public void setTermMonths(int termMonths) { this.termMonths = termMonths; }
    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }
    public BigDecimal getOfferedRate() { return offeredRate; }
    public void setOfferedRate(BigDecimal offeredRate) { this.offeredRate = offeredRate; }
    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }
    public String getDeclineReason() { return declineReason; }
    public void setDeclineReason(String declineReason) { this.declineReason = declineReason; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
}
