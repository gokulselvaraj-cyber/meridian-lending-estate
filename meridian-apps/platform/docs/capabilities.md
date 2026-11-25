# Business capabilities: Meridian Lending

| Capability | What it covers | Where |
|---|---|---|
| Origination | Taking in applications, the portal wizard | loan-portal, loan-core `ApplicationController`, `OriginationService` |
| Underwriting | Bureau score, eligibility rules, the decision | loan-core `UnderwritingService`, `EligibilityPolicy` |
| Pricing | Rates by credit score and LTV | loan-core `RateEngine`, `QuoteService` |
| Servicing | Disbursement, schedules, payments, nightly accrual | loan-core, loan-batch `ACCRUE01` |
| Collections | Late fees, missed payments, reminders | loan-core `LateFeePolicy`, loan-batch `LATEFEE1`, notify-svc `dunning` |
| Customer Communications | Welcome emails, reminders, hardship letters | notify-svc |
| Reporting | Daily customer statements | loan-batch `STMTGEN1`, notify-svc `statement_loader` |
| Broker Partnerships | Loans introduced by brokers and their commission | loan-core `BrokerCommission` |
