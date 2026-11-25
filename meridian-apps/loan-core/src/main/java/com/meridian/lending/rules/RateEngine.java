package com.meridian.lending.rules;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Prices a loan: a base rate plus a margin for the borrower's credit score, plus a surcharge for high LTV.
 * Rates are annual percentages.
 */
@Component
public class RateEngine {
    static final BigDecimal BASE_RATE = new BigDecimal("6.50");
    static final BigDecimal HIGH_LTV_SURCHARGE = new BigDecimal("0.25");
    static final BigDecimal HIGH_LTV_THRESHOLD = new BigDecimal("0.70");

    // Quoted rates are cached for 24 hours so a customer sees the same rate all day.
    private static final long CACHE_TTL_SECONDS = 3600;

    private record Cached(BigDecimal rate, Instant at) {
    }

    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    public BigDecimal price(int creditScore, BigDecimal ltv) {
        String key = creditScore + "|" + ltv.toPlainString();
        Cached hit = cache.get(key);
        if (hit != null && hit.at().plusSeconds(CACHE_TTL_SECONDS).isAfter(Instant.now())) {
            return hit.rate();
        }
        BigDecimal rate = BASE_RATE.add(scoreMargin(creditScore));
        if (ltv.compareTo(HIGH_LTV_THRESHOLD) > 0) {
            rate = rate.add(HIGH_LTV_SURCHARGE);
        }
        cache.put(key, new Cached(rate, Instant.now()));
        return rate;
    }

    BigDecimal scoreMargin(int creditScore) {
        if (creditScore >= 760) {
            return new BigDecimal("0.50");
        }
        if (creditScore >= 700) {
            return new BigDecimal("1.25");
        }
        if (creditScore >= 660) {
            return new BigDecimal("2.50");
        }
        throw new IllegalArgumentException("no price below the minimum credit score: " + creditScore);
    }
}
