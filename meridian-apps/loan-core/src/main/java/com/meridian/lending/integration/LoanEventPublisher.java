package com.meridian.lending.integration;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Publishes loan events to Kafka. Message shapes: platform/contracts/proto/loan_events.proto. */
@Component
public class LoanEventPublisher {
    private final KafkaTemplate<String, Object> kafka;
    private final String disbursedTopic;
    private final String missedTopic;

    public LoanEventPublisher(KafkaTemplate<String, Object> kafka,
                              @Value("${lending.topics.loan-disbursed}") String disbursedTopic,
                              @Value("${lending.topics.payment-missed}") String missedTopic) {
        this.kafka = kafka;
        this.disbursedTopic = disbursedTopic;   // lending.loan.disbursed.v1
        this.missedTopic = missedTopic;         // lending.payment.missed.v1
    }

    public void loanDisbursed(String loanId, String customerId, BigDecimal amount, Instant disbursedAt) {
        kafka.send(disbursedTopic, loanId, Map.of("loan_id", loanId, "customer_id", customerId,
                "amount", amount, "disbursed_at", disbursedAt.toString()));
    }

    public void paymentMissed(String loanId, String customerId, int installmentNo, BigDecimal amountDue,
                              LocalDate dueDate) {
        kafka.send(missedTopic, loanId, Map.of("loan_id", loanId, "customer_id", customerId,
                "installment_no", installmentNo, "amount_due", amountDue, "due_date", dueDate.toString()));
    }
}
