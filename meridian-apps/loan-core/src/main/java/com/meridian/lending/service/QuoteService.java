package com.meridian.lending.service;

import com.meridian.lending.rules.RateEngine;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

/** An indicative rate before the customer applies. No bureau call: the customer states their score band. */
@Service
public class QuoteService {
    private final RateEngine rates;

    public QuoteService(RateEngine rates) {
        this.rates = rates;
    }

    public BigDecimal quote(int statedScore, BigDecimal amount, BigDecimal propertyValue) {
        BigDecimal ltv = amount.divide(propertyValue, 4, RoundingMode.HALF_UP);
        return rates.price(statedScore, ltv);
    }
}
