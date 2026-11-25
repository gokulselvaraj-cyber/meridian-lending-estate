package com.meridian.lending.service.accrual;

import java.time.LocalDate;

/** Adds one day's interest to every active loan. Two implementations while accrual moves off the mainframe. */
public interface AccrualEngine {
    void accrue(LocalDate businessDate);
}
