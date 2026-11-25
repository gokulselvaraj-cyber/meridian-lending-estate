# Runbook: nightly accrual batch recovery

Alert: `LoanAccrualBatchLate` (the nightly batch has not finished by 06:00). Owner: mainframe-ops.

## The job

`NIGHTLY` runs three steps in order:

1. `STEP010` `ACCRUE01`: adds the day's interest to every active loan
2. `STEP020` `LATEFEE1`: charges late fees
3. `STEP030` `STMTGEN1`: writes the statement dataset `MERIDIAN.LENDING.STMT.DAILY`

## If a step abends

1. Check the job log for the failing step and its return code.
2. Fix the cause (usually a locked `LOAN` row or a full dataset).
3. Restart the job from the failing step: `RESTART=STEPnnn` on the job card. Steps before it do not run again.
4. Tell customer-comms if statements will be late: notify-svc reads them at start-up.

## Contacts

`#mainframe-ops` on Slack.
