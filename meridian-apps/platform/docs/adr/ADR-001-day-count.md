# ADR-001: Interest day-count convention

- Status: Accepted
- Date: 2024-02-12
- Deciders: lending-core, mainframe-ops, finance

## Context

Interest was calculated in more than one place, and the conventions had to be made the same.

## Decision

All interest at Meridian Lending is calculated **Actual/365**: the actual number of days elapsed, over a 365-day
year. This applies to daily accrual, statements and every customer-facing estimate.

## Consequences

- One convention everywhere, so a customer's statement and the amounts quoted to them agree.
- `InterestCalculator` in loan-core is the reference implementation.
