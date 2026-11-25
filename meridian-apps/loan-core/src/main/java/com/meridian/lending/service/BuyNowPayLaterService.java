package com.meridian.lending.service;

import java.math.BigDecimal;

/** Buy now, pay later: planned for 2027. Not built yet. */
public class BuyNowPayLaterService {
    public String createPlan(String customerId, BigDecimal purchaseAmount, int instalments) {
        throw new UnsupportedOperationException("buy now, pay later is not available yet");
    }

    public BigDecimal remaining(String planId) {
        throw new UnsupportedOperationException("buy now, pay later is not available yet");
    }
}
