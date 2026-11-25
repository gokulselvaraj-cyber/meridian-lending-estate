package com.meridian.lending.pricing;

/**
 * The plans a borrower can be on.
 * STANDARD: no extras.
 * PLUS: the first late fee in each calendar year is waived.
 */
public enum ProductPlan {
    STANDARD(false),
    PLUS(true);

    private final boolean waivesFirstLateFeeEachYear;

    ProductPlan(boolean waivesFirstLateFeeEachYear) {
        this.waivesFirstLateFeeEachYear = waivesFirstLateFeeEachYear;
    }

    public boolean waivesFirstLateFeeEachYear() {
        return waivesFirstLateFeeEachYear;
    }
}
