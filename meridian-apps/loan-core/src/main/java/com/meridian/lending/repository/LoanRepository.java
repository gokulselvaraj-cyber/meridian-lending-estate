package com.meridian.lending.repository;

import com.meridian.lending.domain.Loan;
import com.meridian.lending.domain.LoanStatus;
import com.meridian.lending.pricing.ProductPlan;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Table LOAN, shared with the mainframe batch (ACCRUE01, LATEFEE1, LTVCHK01) and CICS (LNINQ01). */
@Repository
public class LoanRepository {
    private static final RowMapper<Loan> ROW = (rs, n) -> {
        Loan l = new Loan();
        l.setLoanId(rs.getString("loan_id"));
        l.setCustomerId(rs.getString("customer_id"));
        l.setPrincipal(rs.getBigDecimal("principal"));
        l.setOutstanding(rs.getBigDecimal("outstanding"));
        l.setAnnualRate(rs.getBigDecimal("annual_rate"));
        l.setAccruedInterest(rs.getBigDecimal("accrued_interest"));
        l.setLtvRatio(rs.getBigDecimal("ltv_ratio"));
        l.setPlan(ProductPlan.valueOf(rs.getString("product_plan")));
        l.setStatus(LoanStatus.valueOf(rs.getString("status")));
        l.setDisbursedOn(rs.getDate("disbursed_on").toLocalDate());
        return l;
    };

    private final JdbcTemplate jdbc;

    public LoanRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Loan> findById(String loanId) {
        return jdbc.query("SELECT * FROM LOAN WHERE loan_id = ?", ROW, loanId).stream().findFirst();
    }

    public List<Loan> findActive() {
        return jdbc.query("SELECT * FROM LOAN WHERE status IN ('ACTIVE', 'DELINQUENT')", ROW);
    }

    public void insert(Loan l) {
        jdbc.update("INSERT INTO LOAN (loan_id, customer_id, principal, outstanding, annual_rate, accrued_interest, "
                        + "ltv_ratio, product_plan, status, disbursed_on) VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?, ?)",
                l.getLoanId(), l.getCustomerId(), l.getPrincipal(), l.getOutstanding(), l.getAnnualRate(),
                l.getLtvRatio(), l.getPlan().name(), l.getStatus().name(), java.sql.Date.valueOf(l.getDisbursedOn()));
    }

    public void reduceOutstanding(String loanId, BigDecimal amount) {
        jdbc.update("UPDATE LOAN SET outstanding = outstanding - ? WHERE loan_id = ?", amount, loanId);
    }

    public void addAccruedInterest(String loanId, BigDecimal interest) {
        jdbc.update("UPDATE LOAN SET accrued_interest = accrued_interest + ? WHERE loan_id = ?", interest, loanId);
    }

    public int lateFeesWaivedThisYear(String loanId) {
        return jdbc.queryForObject("SELECT late_fees_waived_ytd FROM LOAN WHERE loan_id = ?", Integer.class, loanId);
    }

    public void chargeLateFee(String loanId, BigDecimal fee) {
        jdbc.update("UPDATE LOAN SET outstanding = outstanding + ? WHERE loan_id = ?", fee, loanId);
    }

    public void recordWaivedLateFee(String loanId) {
        jdbc.update("UPDATE LOAN SET late_fees_waived_ytd = late_fees_waived_ytd + 1 WHERE loan_id = ?", loanId);
    }

    public int purgeClosedBefore(java.time.LocalDate cutoff) {
        return jdbc.update("DELETE FROM LOAN WHERE status = 'CLOSED' AND closed_on < ?", java.sql.Date.valueOf(cutoff));
    }

    public void setStatus(String loanId, LoanStatus status) {
        jdbc.update("UPDATE LOAN SET status = ? WHERE loan_id = ?", status.name(), loanId);
    }
}
