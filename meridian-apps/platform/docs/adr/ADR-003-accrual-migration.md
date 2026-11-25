# ADR-003: Moving the nightly accrual off the mainframe

- Status: Accepted, in progress
- Date: 2025-09-22
- Deciders: lending-core, mainframe-ops

## Context

The nightly accrual runs as COBOL program `ACCRUE01` in mainframe job `NIGHTLY`. Mainframe capacity is being
reduced, and the accrual is the first workload to move.

## Decision

Introduce interface `AccrualEngine` in loan-core with two implementations:

- `CobolAccrualBridge`: requests job `NIGHTLY`, so `ACCRUE01` keeps running as today.
- `JavaAccrualJob`: accrues in Java.

Flag `lending.accrual.engine` (`COBOL` or `JAVA`) picks one at start-up. It stays `COBOL` until both engines have
produced the same figures for a full quarter in parallel runs.

## Consequences

- Late fees (`LATEFEE1`) and statements (`STMTGEN1`) stay on the mainframe for now.
- Switching the flag is a change in `application.yml` and a restart.
