package com.meridian.lending.repository;

import com.meridian.lending.domain.RepaymentSchedule;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Table REPAYMENT_SCHEDULE. */
@Repository
public class ScheduleRepository {
    private final JdbcTemplate jdbc;

    public ScheduleRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void save(RepaymentSchedule s) {
        for (RepaymentSchedule.Installment i : s.getInstallments()) {
            jdbc.update("INSERT INTO REPAYMENT_SCHEDULE (loan_id, installment_no, due_date, amount_due, paid) "
                            + "VALUES (?, ?, ?, ?, ?)",
                    s.getLoanId(), i.installmentNo(), java.sql.Date.valueOf(i.dueDate()), i.amountDue(), i.paid());
        }
    }

    public List<RepaymentSchedule.Installment> findOverdue(java.time.LocalDate today) {
        return jdbc.query("SELECT * FROM REPAYMENT_SCHEDULE WHERE paid = false AND due_date < ?",
                (rs, n) -> new RepaymentSchedule.Installment(rs.getInt("installment_no"),
                        rs.getDate("due_date").toLocalDate(), rs.getBigDecimal("amount_due"), false),
                java.sql.Date.valueOf(today));
    }

    public String loanIdOf(int installmentNo, java.time.LocalDate dueDate) {
        return jdbc.queryForObject("SELECT loan_id FROM REPAYMENT_SCHEDULE WHERE installment_no = ? AND due_date = ?",
                String.class, installmentNo, java.sql.Date.valueOf(dueDate));
    }
}
