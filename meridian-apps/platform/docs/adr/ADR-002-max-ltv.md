# ADR-002: Maximum loan-to-value

- Status: Accepted
- Date: 2024-06-03
- Deciders: credit risk, lending-core

## Context

The risk committee set a single maximum loan-to-value (LTV) for all mortgage lending.

## Decision

The maximum LTV is **80%** everywhere: at underwriting, at refinance, and in the mainframe's nightly LTV check.

## Consequences

- Applications above 80% LTV are declined.
- The nightly `LTVCHK` job flags existing loans above 80% for review.
