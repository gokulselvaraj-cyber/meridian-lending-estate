# Compliance notes: Meridian Lending

## Personal data

Customer records hold personal data: national id, date of birth, email, phone and address. In loan-core these
fields carry the `@Pii` annotation and the national id is masked to its last four characters in logs.

**No personal identifier (national id, date of birth) may be sent to any service outside Meridian's own systems**,
including external AI services. Use the customer id instead.

## Retention

Loan records, including payments and schedules, are kept for **10 years** after the loan closes, then purged.

## Marketing

Marketing messages go only to customers with `marketing_consent` set (Liquibase change 003).
