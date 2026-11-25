      *****************************************************************
      * LOANREC - LOAN RECORD (TABLE LOAN)                            *
      * ACCT-NO IS THE LOAN ACCOUNT NUMBER (LOAN.LOAN_ID), PRINTED ON *
      * STATEMENTS.                                                   *
      *****************************************************************
       01  LOAN-RECORD.
           05  ACCT-NO                 PIC X(10).
           05  LN-CUSTOMER-ID          PIC X(12).
           05  LN-PRINCIPAL            PIC S9(10)V99 COMP-3.
           05  LN-OUTSTANDING          PIC S9(10)V99 COMP-3.
           05  LN-ANNUAL-RATE          PIC S9(3)V99  COMP-3.
           05  LN-ACCRUED-INT          PIC S9(10)V99 COMP-3.
           05  LN-LTV-RATIO            PIC S9V9(4)   COMP-3.
           05  LN-PLAN                 PIC X(10).
           05  LN-STATUS               PIC X(12).
           05  LN-DISBURSED-ON         PIC 9(8).
