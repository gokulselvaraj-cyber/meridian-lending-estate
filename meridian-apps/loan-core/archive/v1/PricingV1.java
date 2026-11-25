package com.meridian.lending.archive.v1;

import java.math.BigDecimal;

/**
 * RETIRED in 2023 when RateEngine replaced it. Kept for audit of loans priced before then; not built or deployed
 * (this folder is outside src/).
 */
public class PricingV1 {
    static final BigDecimal BASE_RATE = new BigDecimal("5.90");

    public BigDecimal price(int creditScore) {
        return creditScore >= 720 ? BASE_RATE : BASE_RATE.add(new BigDecimal("1.00"));
    }
}
