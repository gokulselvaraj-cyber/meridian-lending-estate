-- Meridian Lending: the shared schema (current state after Liquibase changes 001 to 003).
-- Used by loan-core (Java), loan-batch (COBOL EXEC SQL and CICS) and notify-svc (Python).

CREATE TABLE CUSTOMER (
    customer_id        VARCHAR(12)   PRIMARY KEY,
    full_name          VARCHAR(120)  NOT NULL,
    national_id        VARCHAR(20)   NOT NULL,
    date_of_birth      DATE          NOT NULL,
    email              VARCHAR(254),
    phone              VARCHAR(20),
    address            VARCHAR(200),
    postcode           VARCHAR(10),
    marketing_consent  BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE LOAN_APPLICATION (
    application_id     VARCHAR(36)   PRIMARY KEY,
    customer_id        VARCHAR(12)   NOT NULL REFERENCES CUSTOMER (customer_id),
    requested_amount   NUMERIC(12,2) NOT NULL,
    property_value     NUMERIC(12,2) NOT NULL,
    term_months        INTEGER       NOT NULL,
    credit_score       INTEGER,
    offered_rate       NUMERIC(5,2),
    status             VARCHAR(12)   NOT NULL,
    decline_reason     VARCHAR(200),
    submitted_at       TIMESTAMP     NOT NULL
);

CREATE TABLE LOAN (
    loan_id            VARCHAR(10)   PRIMARY KEY,
    customer_id        VARCHAR(12)   NOT NULL REFERENCES CUSTOMER (customer_id),
    principal          NUMERIC(12,2) NOT NULL,
    outstanding        NUMERIC(12,2) NOT NULL,
    annual_rate        NUMERIC(5,2)  NOT NULL,
    accrued_interest   NUMERIC(12,2) NOT NULL DEFAULT 0,
    ltv_ratio          NUMERIC(5,4),
    product_plan       VARCHAR(10)   NOT NULL DEFAULT 'STANDARD',
    status             VARCHAR(12)   NOT NULL,
    disbursed_on       DATE          NOT NULL,
    closed_on          DATE,
    late_fees_waived_ytd INTEGER     NOT NULL DEFAULT 0
);

CREATE TABLE PAYMENT (
    payment_id         VARCHAR(36)   PRIMARY KEY,
    loan_id            VARCHAR(10)   NOT NULL REFERENCES LOAN (loan_id),
    amount             NUMERIC(12,2) NOT NULL,
    received_at        TIMESTAMP     NOT NULL,
    recorded_by        VARCHAR(64)   NOT NULL
);

CREATE TABLE REPAYMENT_SCHEDULE (
    loan_id            VARCHAR(10)   NOT NULL REFERENCES LOAN (loan_id),
    installment_no     INTEGER       NOT NULL,
    due_date           DATE          NOT NULL,
    amount_due         NUMERIC(12,2) NOT NULL,
    paid               BOOLEAN       NOT NULL DEFAULT FALSE,
    PRIMARY KEY (loan_id, installment_no)
);

-- Who changed what, written by every service. Holds ids as plain text on purpose: no foreign keys.
CREATE TABLE AUDIT_EVENT (
    event_id           BIGSERIAL     PRIMARY KEY,
    occurred_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor              VARCHAR(64)   NOT NULL,
    entity_type        VARCHAR(32)   NOT NULL,
    entity_id          VARCHAR(36)   NOT NULL,
    action             VARCHAR(32)   NOT NULL,
    detail             TEXT
);

CREATE INDEX ix_loan_customer ON LOAN (customer_id);
CREATE INDEX ix_schedule_due ON REPAYMENT_SCHEDULE (due_date) WHERE paid = FALSE;
