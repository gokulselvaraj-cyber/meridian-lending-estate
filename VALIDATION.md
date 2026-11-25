# Meridian Lending: SOCK validation questions and expected answers

The answer key for the synthetic estate Meridian Lending, built twice from the same code: `standard/` (the standard
recipe) and `wide/` (stage 07 widened). Both use the same answer key. **Keep this file out of the zips** so the build
cannot read the answers. The code is generated to match this file exactly; if the two ever disagree, the code is wrong, not this key.

How to use it:

- Ask each question in the viewer's **Ask** tab (or `POST` to the ask service), and check structural ones in the
  **Graph** and **Documents** tabs.
- Score each row: **Pass** (the expected answer, with evidence pointing at the listed files), **Partial** (right answer,
  wrong or missing evidence; or only one side of a conflict), **Fail** (wrong, invented, or missing).
- Rows marked **conflict** pass only when the SOCK reports **both** sides and says they disagree. Picking one side is a
  Partial.
- Rows marked **not in estate** pass only when the SOCK says it does not know. Any confident answer is a Fail
  (it was invented).
- Known msock-dev gap: a version published from the console has no Ask search index yet, so Ask may answer "not known"
  to everything. Then check the same facts in the Graph tab (search the node, open its evidence).

## The estate in one view

| Repo (top folder) | Language | What it is |
|---|---|---|
| `loan-core` | Java (Spring style) | Origination, underwriting, pricing, servicing. Layers: `controller` → `service` → `domain` / `rules` → `repository` |
| `loan-batch` | COBOL, copybooks, JCL | Legacy nightly accrual, late fees, LTV check, statements |
| `loan-db` | SQL DDL, Liquibase | The shared schema |
| `loan-portal` | TypeScript | The customer's application screens |
| `notify-svc` | Python | Welcome and dunning messages, statement loader |
| `platform` | Terraform, Kubernetes, Helm, CI, OpenAPI, protobuf, Prometheus rules, CODEOWNERS, SBOM, docs | Infra, contracts, ownership, decisions |

The business journey: **apply → quote → underwrite → disburse → service (accrue, pay) → collect (late fee, dunning)**.

---

## D01 Call chain

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 1 | What does `POST /api/v1/applications` call, down to the database? | `ApplicationController.submit` → `OriginationService.submit` → `UnderwritingService.assess` → `EligibilityPolicy.check` and `RateEngine.price` → `ApplicationRepository.save` (table `LOAN_APPLICATION`) | `loan-core/.../controller/ApplicationController.java`, `service/OriginationService.java`, `service/UnderwritingService.java` | fact |
| 2 | Which services call `RateEngine`? | **5**: `QuoteService`, `UnderwritingService`, `DisbursementService`, `RepaymentScheduleService`, `RefinanceService` | `loan-core/.../service/*.java` | 1:N |
| 3 | Which programs does `ACCRUE01` call? | `INTCALC1` (interest) and `DATEUTL1` (date arithmetic). The `INTCALC1` call is **dynamic**: `CALL WS-INT-PGM`, where `WS-INT-PGM` holds `'INTCALC1'` | `loan-batch/cobol/ACCRUE01.cbl` | fact (dynamic call) |
| 4 | Which programs call `DATEUTL1`? | **3**: `ACCRUE01`, `LATEFEE1`, `STMTGEN1` | `loan-batch/cobol/*.cbl` | 1:N |
| 5 | In what order does the nightly batch run? | `NIGHTLY` job: `STEP010` `ACCRUE01` → `STEP020` `LATEFEE1` → `STEP030` `STMTGEN1`. `LTVCHK01` runs in a separate job, `LTVCHK` | `loan-batch/jcl/NIGHTLY.jcl`, `LTVCHK.jcl` | fact |

## D02 Contract surface

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 6 | List the REST operations `loan-core` implements. | **5**: `POST /api/v1/applications`, `GET /api/v1/applications/{id}`, `POST /api/v1/quotes`, `GET /api/v1/loans/{loanId}`, `POST /api/v1/loans/{loanId}/payments` | `loan-core/.../controller/*.java` | fact |
| 7 | Does the OpenAPI spec match the implementation? | **No.** The spec also lists `DELETE /api/v1/applications/{id}`, which no controller implements | `platform/contracts/openapi/loan-api.yaml` vs controllers | conflict |
| 8 | Which event messages are defined, and their fields? | `LoanDisbursed` (`loan_id`, `customer_id`, `amount`, `disbursed_at`) and `PaymentMissed` (`loan_id`, `customer_id`, `installment_no`, `amount_due`, `due_date`) | `platform/contracts/proto/loan_events.proto` | fact |

## D03 Interfaces and integration

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 9 | Which Kafka topics exist; who produces and who consumes each? | `lending.loan.disbursed.v1`: produced by `loan-core` (`LoanEventPublisher`, called from `DisbursementService`), consumed by `notify-svc` (`consumers.py`). `lending.payment.missed.v1`: produced by `loan-core` (`LoanEventPublisher`, called from `RepaymentScheduleService`), consumed by `notify-svc` | `loan-core/.../integration/LoanEventPublisher.java`, `notify-svc/notify/consumers.py` | fact |
| 10 | Which external system does `loan-core` call, and with what timeout and retries? | Credit bureau at `https://bureau.example.invalid/v2/score`, through `CreditBureauClient`: timeout **3 s**, **2** retries | `loan-core/.../integration/CreditBureauClient.java`, `application.yml` | fact |
| 11 | How do `loan-batch` and `notify-svc` exchange data? | By file: `STMTGEN1` writes dataset `MERIDIAN.LENDING.STMT.DAILY`; it is exported to `/data/stmt/daily.dat`, which `notify-svc`'s `statement_loader.py` reads | `loan-batch/jcl/NIGHTLY.jcl`, `notify-svc/notify/statement_loader.py` | fact |

## D04 Business journey

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 12 | Describe the loan journey end to end. | Apply (portal) → quote (`QuoteService`) → underwrite (`UnderwritingService`: eligibility, bureau score, price) → disburse (`DisbursementService`, emits `LoanDisbursed`) → service (nightly accrual `ACCRUE01`; payments via `POST /loans/{id}/payments`) → collect (late fee `LATEFEE1` / `LateFeePolicy`; missed payment emits `PaymentMissed`; `notify-svc` dunning) | across repos | fact |
| 13 | When does a customer get dunning reminders? | On days **1, 7 and 30** after a `PaymentMissed` event | `notify-svc/notify/dunning.py` | fact |

## D05 UI flow

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 14 | What are the steps of the application wizard? | `/apply/personal` → `/apply/property` → `/apply/review` → `/apply/submitted` | `loan-portal/src/routes.ts`, `src/pages/apply/*` | fact |
| 15 | Which backend calls does the portal make, from which screens? | `/apply/property` calls `POST /api/v1/quotes`; `/apply/review` calls `POST /api/v1/applications`; `/loans/:id` calls `GET /api/v1/loans/{loanId}`; `/loans/:id/pay` calls `POST /api/v1/loans/{loanId}/payments` | `loan-portal/src/api/client.ts`, pages | fact |

## D06 As-is archaeology

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 16 | Is there dead code? | Yes: `LegacyFeeCalculator` (Java) is referenced by nothing; `LATEFEE1` has a paragraph `OLD-FEE-CALC` that is never `PERFORM`ed | `loan-core/.../rules/LegacyFeeCalculator.java`, `loan-batch/cobol/LATEFEE1.cbl` | fact |

## D07 Legacy estate map

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 17 | Which parts are legacy mainframe, and what do they share with the modern stack? | `loan-batch`: **6** batch COBOL programs (`ACCRUE01`, `INTCALC1`, `DATEUTL1`, `LATEFEE1`, `LTVCHK01`, `STMTGEN1`), **1** CICS online program (`LNINQ01`, transaction `LNIQ`, 3270 map `LNINQM`), 2 copybooks (`CUSTMAST`, `LOANREC`), 2 JCL jobs (`NIGHTLY`, `LTVCHK`), 1 parameter member (`PARMLIB/ACCRPARM`). They share the `LOAN` and `CUSTOMER` tables with `loan-core` | `loan-batch/**`, `loan-db/schema.sql` | fact |
| 18 | Which programs use copybook `CUSTMAST`? | **4**: `ACCRUE01`, `LATEFEE1`, `LTVCHK01`, `STMTGEN1` | `loan-batch/copybook/CUSTMAST.cpy` and `COPY CUSTMAST` lines | 1:N |

## D08 Domain model

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 19 | What are the core domain entities? | `Customer`, `LoanApplication`, `Loan`, `Payment`, `RepaymentSchedule` (Java); `CUSTMAST` and `LOANREC` records (COBOL) | `loan-core/.../domain/*.java`, `loan-batch/copybook/*.cpy` | fact |
| 20 | What states can a loan be in? | `APPLIED`, `APPROVED`, `DECLINED`, `DISBURSED`, `ACTIVE`, `DELINQUENT`, `CLOSED` | `loan-core/.../domain/LoanStatus.java` | fact |

## D09 Data relation

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 21 | How are the tables related? | `LOAN.customer_id` → `CUSTOMER`; `LOAN_APPLICATION.customer_id` → `CUSTOMER`; `PAYMENT.loan_id` → `LOAN`; `REPAYMENT_SCHEDULE.loan_id` → `LOAN`. `AUDIT_EVENT` has no foreign key | `loan-db/schema.sql` | fact |
| 22 | Who reads or writes table `LOAN`? | **6**: `LoanRepository` (Java), `ACCRUE01`, `LATEFEE1`, `LTVCHK01` (COBOL batch, `EXEC SQL`), `LNINQ01` (CICS, read only), `dunning.py` (Python, read only). The one-off tool `backfill_ltv.py` also writes it, but is tooling, not runtime (see #72) | across repos | 1:N |
| 23 | Which schema changes are recorded? | Liquibase `001-init`, `002-add-ltv-column` (adds `LOAN.ltv_ratio`), `003-add-consent` (adds `CUSTOMER.marketing_consent`) | `loan-db/changelog/*.xml` | fact |

## D10 Data lineage

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 24 | Where does the interest shown in a dunning reminder come from? | `LOAN` → `ACCRUE01` (via `INTCALC1`) updates `LOAN.accrued_interest` and writes the accrual extract `MERIDIAN.LENDING.ACCRUAL.DAILY` → `STMTGEN1` reads it and writes `MERIDIAN.LENDING.STMT.DAILY` → `/data/stmt/daily.dat` → `statement_loader.py` → `dunning.py`. Separately `notify-svc` recomputes "interest so far" with its own `interest.py` | as above | fact |

## D11 Business rules (the planted conflicts are here)

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 25 | What is the maximum loan-to-value (LTV)? | **Three different values:** `EligibilityPolicy.MAX_LTV = 0.80` (used by `UnderwritingService`); `lending.max-ltv: 0.75` in `application.yml` (used only by `RefinanceService`); `LTVCHK01` allows up to **85%**. ADR-002 claims 80% everywhere | `rules/EligibilityPolicy.java`, `application.yml`, `LTVCHK01.cbl`, `platform/docs/adr/ADR-002-*.md` | conflict |
| 26 | What is the minimum credit score? | **Two values:** server `EligibilityPolicy.MIN_CREDIT_SCORE = 660`; portal pre-check `MIN_CREDIT_SCORE = 640` | `rules/EligibilityPolicy.java`, `loan-portal/src/rules/eligibility.ts` | conflict |
| 27 | How long is the late-fee grace period? | **Two values:** Java `LateFeePolicy.GRACE_DAYS = 10`; COBOL **15** in effect: `LATEFEE1` defaults `WS-GRACE-DAYS` to 14, but the `NIGHTLY` job passes parameter member `ACCRPARM` (`GRACE-DAYS=15`), which overrides it. Answering 14 for COBOL is a Partial | `rules/LateFeePolicy.java`, `LATEFEE1.cbl`, `loan-batch/PARMLIB/ACCRPARM` (no file extension), `NIGHTLY.jcl` | conflict |
| 28 | How is a late fee calculated? | 5% of the overdue installment, minimum 15.00 (same in Java and COBOL) | as above | fact |
| 29 | Which day-count convention is used for interest? | **Two:** Java `InterestCalculator` uses **Actual/365**; COBOL `INTCALC1` and Python `interest.py` use **30/360**. ADR-001 claims Actual/365 | `rules/InterestCalculator.java`, `INTCALC1.cbl`, `notify-svc/notify/interest.py`, ADR-001 | conflict |
| 30 | How is the interest rate priced? | Base **6.50%** plus a margin by credit score: ≥ 760 **+0.50**, 700–759 **+1.25**, 660–699 **+2.50**; plus **+0.25** when LTV > 0.70 | `rules/RateEngine.java` | fact |
| 31 | Worked example: score 720, LTV 0.65. What rate? | **7.75%** (6.50 + 1.25) | `RateEngine.java` | calc |
| 32 | Worked example: score 680, LTV 0.75. What rate? | **9.25%** (6.50 + 2.50 + 0.25) | `RateEngine.java` | calc |
| 33 | One month's interest on 100,000 at 7.75%: Java vs COBOL? | Java, 31 days Actual/365: **658.22**. COBOL, 30/360: **645.83**. The systems disagree by 12.39 | `InterestCalculator.java`, `INTCALC1.cbl` | conflict + calc |
| 34 | An application with score 650 and LTV 0.82: what happens? | The portal pre-check **passes** it (≥ 640); the server **declines** it on both rules (< 660 and > 0.80). The batch `LTVCHK01` would **not** flag the LTV (≤ 0.85) | as 25, 26 | conflict |
| 35 | A 1,200.00 installment paid 12 days late: what fee? | Java: **60.00** (past 10 days). COBOL: **none** (within 15 days) | as 27, 28 | conflict + calc |

## D12 Compliance footprint

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 36 | Which fields hold personal data? | `Customer.nationalId` (masked to its last 4 characters when logged), `dateOfBirth`, `email`, `phone`, `address`; marked `@Pii`. Mirrored in `CUSTMAST` (`CM-NATIONAL-ID`, `CM-DOB`) | `domain/Customer.java`, `CUSTMAST.cpy` | fact |
| 37 | How long are loan records kept? | **Conflict:** `RetentionPolicy.RETENTION_YEARS = 7` in code; the compliance doc says **10 years** | `rules/RetentionPolicy.java`, `platform/docs/compliance.md` | conflict |

## D13 Security posture

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 38 | Which roles exist and what can each do? | `APPLICANT` (submit and view own applications), `UNDERWRITER` (view applications), `SERVICING_AGENT` (record payments), `ADMIN` (all). `POST /loans/{id}/payments` needs `SERVICING_AGENT` | `loan-core/.../config/SecurityConfig.java` | fact |
| 39 | Is any endpoint open without sign-in? | Yes: `POST /api/v1/quotes` is `permitAll` (anonymous) | `SecurityConfig.java` | fact (risk) |
| 40 | How are database credentials supplied? | From environment variables `${LOAN_DB_USER}` and `${LOAN_DB_PASSWORD}`; no secret is written in the code | `application.yml`, `platform/k8s/loan-core.yaml` | fact |

## D14 Organization and ownership

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 41 | Who owns each repo? | `loan-core` lending-core; `loan-batch` mainframe-ops; `loan-db` data-platform; `loan-portal` digital; `notify-svc` **conflict**: customer-comms in `CODEOWNERS`, digital in the service catalogue; `platform` sre | `platform/CODEOWNERS`, `platform/teams.yaml`, `platform/catalog-info.yaml` | fact + conflict |

## D15 Infra inventory

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 42 | What cloud resources are declared? | PostgreSQL instance `meridian-lending-db`, Kafka cluster `meridian-events`, storage bucket `meridian-lending-statements` | `platform/terraform/main.tf` | fact |
| 43 | What runs on Kubernetes, with how many replicas? | `loan-core` **3**, `loan-portal` **2**, `notify-svc` **1** | `platform/k8s/*.yaml` | fact |

## D16 Ops inventory

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 44 | What alerts exist? | `LoanAccrualBatchLate` (nightly batch not finished by 06:00) and `HighPaymentFailureRate` (> 5% failed payments over 15 min) | `platform/observability/alerts.yaml` | fact |
| 45 | Which runbooks exist? | One: accrual batch recovery | `platform/docs/runbooks/accrual-batch.md` | fact |

## D17 Migration seams

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 46 | How is accrual moving off the mainframe? | Interface `AccrualEngine` has two implementations: `CobolAccrualBridge` (submits the `NIGHTLY` job, so `ACCRUE01` runs) and `JavaAccrualJob`. Flag `lending.accrual.engine` (`COBOL` or `JAVA`, default **`COBOL`**) picks one at start-up. ADR-003 records the plan | `application.yml`, `service/accrual/*.java`, ADR-003 | fact (dynamic dispatch) |
| 47 | What would change for customers if the flag flipped to `JAVA`? | Interest switches from 30/360 to Actual/365 (see #29 and #33) | inference from #29, #46 | inference |

## D18 Capability map

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 48 | What business capabilities does the estate have? | **8**: Origination, Underwriting, Pricing, Servicing, Collections, Customer Communications, Reporting (statements), Broker Partnerships | `platform/docs/capabilities.md`, `platform/catalog-info.yaml`, module names | fact |

## D19 Reliability map

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 49 | Which services have health checks? | `loan-core` (readiness and liveness on `/actuator/health`) and `loan-portal`. `notify-svc` has **none**, and runs 1 replica: a single point of failure | `platform/k8s/*.yaml` | fact (gap) |

## D20 Quality and testing

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 50 | Which business rules have tests, and which do not? | Tested: `InterestCalculator`, `EligibilityPolicy`. **Not tested:** `RateEngine`, `LateFeePolicy`, all COBOL, all of `notify-svc` | `loan-core/src/test/**` | fact (gap) |
| 51 | What does CI build and test? | `loan-core` (build and unit tests) and `loan-portal` (build). **`notify-svc` has no CI job** | `platform/ci/ci.yml` | fact (gap) |

## D21 Provenance and confidence

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 52 | How long are rates cached? | **Conflict:** the comment says 24 hours; the code caches for **3600 s (1 hour)** | `rules/RateEngine.java` | conflict |
| 53 | Which documents disagree with the code? | ADR-001 (day count), ADR-002 (max LTV), the compliance doc (retention), the `RateEngine` comment (cache), the OpenAPI spec (`DELETE` operation) | as above | conflict |

## Duplicate code (checked across dimensions)

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 54 | Is the interest calculation implemented more than once? | **3** times: Java `InterestCalculator`, COBOL `INTCALC1`, Python `interest.py`. Same formula, two day-count conventions | as #29 | duplicate |
| 55 | Is address validation duplicated? | **2** copies that have drifted: Java `AddressValidator` accepts a 5-digit postcode only; TypeScript `addressValidator.ts` accepts 5 or 9 digits (`12345-6789`) | `loan-core/.../rules/AddressValidator.java`, `loan-portal/src/rules/addressValidator.ts` | duplicate + conflict |

## Not in the estate: the answer must be "not known"

| # | Question | Expected answer |
|---|---|---|
| 56 | What is the loan origination fee? | Not known (there is none in the estate) |
| 57 | Which payment gateway processes card payments? | Not known |
| 58 | What is the maximum loan term in months? | Not known |
| 59 | Which mobile app screens exist? | Not known (there is no mobile app) |

## Structure checks (Graph tab, not Ask)

| # | Check | Expected |
|---|---|---|
| 60 | Every repo appears | 6 top-level components: `loan-core`, `loan-batch`, `loan-db`, `loan-portal`, `notify-svc`, `platform` |
| 61 | Readers ran on every language | Stage 04 jobs from at least: Java, COBOL, JCL, DDL, Liquibase, TypeScript, Python, Terraform, Kubernetes, OpenAPI, protobuf, CI, CODEOWNERS |
| 62 | Nothing planted is missing | Every class, program, table, topic and route named above is a node with evidence pointing at its file |
| 63 | Links run the right way | `UnderwritingService` → `RateEngine` (not the reverse); `ACCRUE01` → `INTCALC1`; `PAYMENT` → `LOAN` |
| 64 | All 21 dimensions have a status | D01–D21 each covered or with a recorded gap; none silently empty |

## Added complexity: scope, identity, AI, commercial

These rows test the stages that decide *what counts* (07's scopes), *what is one thing* (09) and *what links to what*
(10). Several deliberately plant a wrong value somewhere that must **not** be reported as the product's truth.

| # | Question | Expected answer | Evidence | Type |
|---|---|---|---|---|
| 65 | What does the PLUS plan change? | `ProductPlan.PLUS` waives the **first late fee in each calendar year**; `STANDARD` waives nothing. Java `LateFeePolicy` checks the plan; COBOL `LATEFEE1` does **not**, so a PLUS customer's first late fee is still charged by the batch | `loan-core/.../pricing/ProductPlan.java`, `rules/LateFeePolicy.java`, `LATEFEE1.cbl` | conflict |
| 66 | How are brokers paid? | `BrokerCommission`: **0.50%** of the loan amount below 250,000; **0.40%** at 250,000 or more | `loan-core/.../pricing/BrokerCommission.java`, `platform/docs/plans.md` | fact |
| 67 | Does the estate use AI? | Yes, once: `notify-svc` drafts hardship letters through an internal AI gateway (`https://ai-gateway.meridian.invalid`), model `letter-writer-v2`, prompt template `prompts/hardship_letter.txt` | `notify-svc/notify/hardship_letter.py`, `llm_client.py`, `prompts/hardship_letter.txt` | fact |
| 68 | Does any personal data leave the bank? | **Yes, against policy:** the hardship-letter prompt includes the customer's `national_id`; `compliance.md` says no personal identifier may be sent to an external service | `prompts/hardship_letter.txt`, `hardship_letter.py`, `platform/docs/compliance.md` | conflict (risk) |
| 69 | What is the base interest rate? | **6.50%** (`RateEngine`). Pass only if 6.50%. `5.90%` is in retired `archive/v1/PricingV1.java` (**historical**), `3.10%` is in a **test fixture**: reporting either as the current rate is a Fail; mentioning them as historical or test data is fine | `rules/RateEngine.java`; decoys `loan-core/archive/v1/PricingV1.java`, `loan-core/src/test/resources/fixtures/loans.json` | scope trap |
| 70 | Does any loan allow LTV 0.95? | **No.** `0.95` appears only in the test fixture `loans.json`. The product values are those in #25 | as #25; decoy `fixtures/loans.json` | scope trap |
| 71 | Does Meridian offer buy-now-pay-later? | **No.** `BuyNowPayLaterService` is a **planned stub**: every method throws `UnsupportedOperationException`, nothing calls it, no endpoint exposes it | `loan-core/.../service/BuyNowPayLaterService.java` | scope trap |
| 72 | What is `backfill_ltv.py`? | A **one-off tool** that filled `LOAN.ltv_ratio` after Liquibase change 002. It is not part of any runtime flow | `platform/tools/backfill_ltv.py` | scope trap |
| 73 | Are `Customer`, `Borrower`, `CUSTMAST` and `CUSTOMER` the same thing? | **Yes**, one business concept: Java `Customer`, TypeScript type `Borrower`, COBOL copybook `CUSTMAST`, table `CUSTOMER` (and `customer_id` in the events) | across repos | identity (merge) |
| 74 | Is the portal's `Account` the same as COBOL's `ACCT-NO`? | **No.** Portal `Account` is a sign-in profile (username, password reset). `ACCT-NO` in `LOANREC` is the loan account number. Merging them is a Fail | `loan-portal/src/types/account.ts`, `loan-batch/copybook/LOANREC.cpy` | identity (keep apart) |
| 75 | There are two `LoanValidator` classes. What does each check? | `origination.LoanValidator`: requested amount between **5,000 and 2,000,000**. `servicing.LoanValidator`: a payment is **> 0 and ≤ the outstanding balance**. `ApplicationController` uses the first, `LoanController` the second | `loan-core/.../origination/LoanValidator.java`, `.../servicing/LoanValidator.java` | identity (same name) |
| 76 | What is the largest loan Meridian will lend? | **2,000,000** (`origination.LoanValidator`) | as #75 | fact |
| 77 | Which accrual implementation runs today? | `CobolAccrualBridge`, because the flag defaults to `COBOL` (see #46). An answer naming only `JavaAccrualJob` is a Fail | as #46 | dynamic dispatch |
| 78 | What does CICS transaction `LNIQ` do? | Program `LNINQ01` shows a loan's balance and status on 3270 screen `LNINQM`; it reads `LOAN` (copybook `LOANREC`) and changes nothing | `loan-batch/cics/LNINQ01.cbl`, `loan-batch/bms/LNINQM.bms` | fact (legacy UI) |
| 79 | Do the event contracts match the code? | **No.** The AsyncAPI file lists a third topic, `lending.loan.closed.v1`, that nothing produces or consumes | `platform/contracts/asyncapi/loan-events.yaml` vs #9 | conflict |
| 80 | Does the architecture diagram match the code? | **No.** `architecture.puml` shows `loan-portal` calling `notify-svc` directly; no code does. The link is documentary only | `platform/docs/architecture.puml` | conflict (documentary vs real) |
| 81 | Is the SBOM current? | **No.** The SBOM lists `jackson-databind 2.13.0`; `pom.xml` uses **2.17.1** | `platform/sbom/sbom.cdx.json`, `loan-core/pom.xml` | conflict |
| 82 | What runs locally for development? | `docker-compose.yml`: `loan-core`, `notify-svc`, PostgreSQL, Kafka. Images: `loan-core` on `eclipse-temurin:17-jre`, `notify-svc` on `python:3.11-slim` | `platform/docker-compose.yml`, `*/Dockerfile` | fact |
| 83 | Which external systems does the estate depend on? | **3**, outside the estate's boundary: credit bureau (`bureau.example.invalid`), SMTP relay (`smtp.meridian.invalid`), AI gateway (`ai-gateway.meridian.invalid`) | `CreditBureauClient.java`, `notify-svc/notify/mailer.py`, `llm_client.py` | boundary |

## The team note (added in the console, not in the zip)

After the source is registered, add this **tacit note** on the SOCK (a person typing what the code cannot say):

> Month-end: ACCRUE01 must finish before STMTGEN1 starts. When the accrual overruns past 05:00 on the last day of the
> month, operators restart from STEP020 and the statements show the previous day's interest. Happened in March and
> June. — mainframe-ops

| # | Question | Expected answer | Type |
|---|---|---|---|
| 84 | What happens at month-end if the accrual batch overruns? | Operators restart from `STEP020`, so statements show the previous day's interest; it happened in March and June. The answer must say this comes from a **team note** (reported by mainframe-ops), not from code | tacit (provenance) |

---

## How the estate exercises each stage

What to look at in the run view (`GET /v1/runs/{run}/view`, the Approvals tab) and the Graph tab, stage by stage.
Stages 05, 07 and 08 are optional in the recipe; the others always run.

| Stage | What it does | What the estate puts in its way | Expect to see |
|---|---|---|---|
| **00 Source ingestion** | Copies each registered source | Two sources (recommended): `meridian-apps` and `meridian-mainframe` (see "Sources" below), plus the team note | Both sources admitted; snapshot ids on both |
| **01 Snapshot** | Freezes the copy; hash and secret-leak scan (`guardrail-secrets`) | Credentials only as `${ENV}` placeholders; no secret-like values (the testbed keeps traps off msock-dev) | Nothing quarantined. A quarantine here is a **false positive** worth a finding |
| **02 Inventory** | Counts what is there; sends each file to a reader, or to the format detectors | Files no reader claims: `PARMLIB/ACCRPARM` (no extension), `prompts/hardship_letter.txt`, `ops/accrual-2026-10.log.txt`; documents: Markdown ADRs and docs, one HTML help page | Every file accounted for; the unclaimed ones typed by the detectors |
| **03 Plan** | Splits into units; **G1** scope sign-off | ~20 formats, so many units across 2 sources | A scope estimate with units per language; then G1 |
| **04 Deterministic parse** | Rule-based readers per language | Java, TypeScript, Python, COBOL, CICS, JCL, DDL, Liquibase, OpenAPI, AsyncAPI, protobuf, Terraform, Kubernetes, Helm, Dockerfile, Compose, CI, CODEOWNERS, Backstage, SBOM, build manifests (`pom.xml`, `package.json`, `pyproject.toml`), app config, Prometheus rules, PlantUML | Stage-04 jobs for **every** format listed (row #61). A format with no jobs is a reader gap |
| **05 Agent reading** | AI writers per topic family | Something for every family: product (plans), domain (rules), architecture (layers), API, events, data, UI, infra, **AI** (#67), security, ops, **commercial** (#65, #66), governance (ADRs), docs, call-chain hops | Writer notes for each family; the docs read as documents |
| **06 Corpus index** | Search index for Ask | The 84 questions | Ask finds evidence. (Console-published versions have no index yet: known gap) |
| **07 Cross-source discovery** | One detector per dimension finds surfaces across all sources; then sorts each into a scope | Journeys and lineage that cross both sources and 4 languages; the scope decoys (#69–72) | Surfaces per dimension in scope; decoys scoped `test-fixture`, `historical`, `planned-stub`, `tooling`, **not** `runtime-product` |
| **08 Deep inference** | One deep write-up per surface, each hop with its cited line; then marks overlaps and which one is canonical | Three interest calculations and two address validators (overlapping surfaces); the 4-language journey | Overlaps recorded; a cited line for every hop of #12 and #24 |
| **09 Entity modelling** | Typed nodes; author and blind verifier; merge or split | Synonyms (#73), a homonym (#74), two classes with one name (#75), `Loan` vs `LoanApplication` (must stay apart), domain terms the ontology may lack ("arrears bucket") | The right merges and splits; every dimension has a status (gate `g4-gap-status-complete`) |
| **10 Relationship linking** | Edges: which node each end is, which relationship, which direction | Dynamic `CALL` (#3), interface with two implementations (#46, #77), producer → topic → consumer, edges to 3 external systems (#83), documentary-only links (#80) | Edges as in #63; external ends marked as outside the boundary; documentary edges not marked impact-bearing |

### The recipe must be widened, or most dimensions are not tested

In the standard recipe (`msock/standard-recipe 2026.10.46`) stage 07 runs detectors for **D01 and D04 only**
(`synthesis_scope: allowlist, kinds: [D01, D04]`, held for cost). Every other dimension is recorded as
`held_by_policy`. To evaluate all 21, this SOCK's product recipe must widen that list to every dimension with a
detector (D01–D13 and D15–D19), through collection step **C. Create recipe version**. That costs more: every kind
in scope adds a detector and its deep write-ups.

D14, D20 and D21 have **no** stage-07 detector. They are judged from stages 04, 05, 09 and 10 (ownership, test and
provenance facts), and their status comes from gate `g4-gap-status-complete`.

With the standard recipe unchanged, the expected result for D02–D19 at stage 07 is **"held by policy"**, not a Fail.

### Sources

**Two zips, two sources** per SOCK. Stage 07 is *cross-source* discovery, and two sources test it. Two SOCKs, same
zips (byte-identical), so their answers compare row for row:

| SOCK | Project | Recipe | Apps source | Mainframe source |
|---|---|---|---|---|
| Meridian Lending Standard | `meridian-std` | standard, unchanged | `meridian-std-apps` | `meridian-std-mainframe` |
| Meridian Lending Wide | `meridian-wide` | `wide/recipe-wide.json` (stage 07: 18 kinds) | `meridian-wide-apps` | `meridian-wide-mainframe` |

| Zip | Contents |
|---|---|
| `meridian-apps.zip` | `loan-core`, `loan-db`, `loan-portal`, `notify-svc`, `platform` |
| `meridian-mainframe.zip` | `loan-batch` |

Both zips are far under 500 KB. Push steps are in each variant's `README.md`; `build.py` rebuilds both from `src/`.
Comparing the two SOCKs answers a separate question: for each D02–D19 row, did widening the recipe turn a *held*
into a *Pass*?

## Scoring summary

Score each row Pass, Partial or Fail. Read the failures by type:

| Type | What a failure points at |
|---|---|
| fact | a reader or extraction problem (stages 04, 05) |
| 1:N | missing or mis-bound edges (stage 10) |
| conflict | the analysis stages (07, 08), or a lens held by policy |
| duplicate | overlap detection (stage 08) |
| calc / inference | the answer layer, or a wrong underlying fact |
| scope trap | stage 07's scoping: test, historical, stub or tooling reported as product truth |
| identity | stage 09 (merge or split) or stage 10 (binding the right node) |
| dynamic call / dispatch | stage 10 binding |
| boundary | stage 10's boundary classification |
| tacit (provenance) | the team-note lane (stage 05) and provenance (D21) |
| not in estate | invention. The most serious failure |
