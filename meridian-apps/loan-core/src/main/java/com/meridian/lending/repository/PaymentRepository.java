package com.meridian.lending.repository;

import com.meridian.lending.domain.Payment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Table PAYMENT. */
@Repository
public class PaymentRepository {
    private final JdbcTemplate jdbc;

    public PaymentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void save(Payment p) {
        jdbc.update("INSERT INTO PAYMENT (payment_id, loan_id, amount, received_at, recorded_by) VALUES (?, ?, ?, ?, ?)",
                p.getPaymentId(), p.getLoanId(), p.getAmount(), java.sql.Timestamp.from(p.getReceivedAt()),
                p.getRecordedBy());
    }
}
