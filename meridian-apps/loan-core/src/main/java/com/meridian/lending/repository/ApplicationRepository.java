package com.meridian.lending.repository;

import com.meridian.lending.domain.LoanApplication;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Table LOAN_APPLICATION. */
@Repository
public class ApplicationRepository {
    private final JdbcTemplate jdbc;

    public ApplicationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public LoanApplication save(LoanApplication a) {
        jdbc.update("INSERT INTO LOAN_APPLICATION (application_id, customer_id, requested_amount, property_value, "
                        + "term_months, credit_score, offered_rate, status, decline_reason, submitted_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                a.getApplicationId(), a.getCustomerId(), a.getRequestedAmount(), a.getPropertyValue(),
                a.getTermMonths(), a.getCreditScore(), a.getOfferedRate(), a.getStatus().name(),
                a.getDeclineReason(), java.sql.Timestamp.from(a.getSubmittedAt()));
        return a;
    }

    public java.util.List<LoanApplication> findApproved() {
        return jdbc.query("SELECT * FROM LOAN_APPLICATION WHERE status = 'APPROVED'", this::map);
    }

    public void markDisbursed(String applicationId) {
        jdbc.update("UPDATE LOAN_APPLICATION SET status = 'DISBURSED' WHERE application_id = ?", applicationId);
    }

    public Optional<LoanApplication> findById(String applicationId) {
        return jdbc.query("SELECT * FROM LOAN_APPLICATION WHERE application_id = ?", this::map, applicationId)
                .stream().findFirst();
    }

    private LoanApplication map(java.sql.ResultSet rs, int n) throws java.sql.SQLException {
        LoanApplication a = new LoanApplication();
        a.setApplicationId(rs.getString("application_id"));
        a.setCustomerId(rs.getString("customer_id"));
        a.setRequestedAmount(rs.getBigDecimal("requested_amount"));
        a.setPropertyValue(rs.getBigDecimal("property_value"));
        a.setTermMonths(rs.getInt("term_months"));
        a.setCreditScore(rs.getInt("credit_score"));
        a.setOfferedRate(rs.getBigDecimal("offered_rate"));
        a.setStatus(com.meridian.lending.domain.LoanStatus.valueOf(rs.getString("status")));
        return a;
    }
}
