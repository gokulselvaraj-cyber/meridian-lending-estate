package com.meridian.lending.domain;

/** The life of a loan, from application to closure. */
public enum LoanStatus {
    APPLIED,
    APPROVED,
    DECLINED,
    DISBURSED,
    ACTIVE,
    DELINQUENT,
    CLOSED
}
