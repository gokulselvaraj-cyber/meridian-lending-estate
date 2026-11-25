package com.meridian.lending.repository;

import com.meridian.lending.domain.Customer;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Table CUSTOMER. */
@Repository
public class CustomerRepository {
    private final JdbcTemplate jdbc;

    public CustomerRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Customer> findById(String customerId) {
        return jdbc.query("SELECT * FROM CUSTOMER WHERE customer_id = ?",
                (rs, n) -> new Customer(rs.getString("customer_id"), rs.getString("full_name"),
                        rs.getString("national_id"), rs.getDate("date_of_birth").toLocalDate(), rs.getString("email"),
                        rs.getString("phone"), rs.getString("address"), rs.getString("postcode"),
                        rs.getBoolean("marketing_consent")),
                customerId).stream().findFirst();
    }
}
