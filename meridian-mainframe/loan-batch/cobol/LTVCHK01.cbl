       IDENTIFICATION DIVISION.
       PROGRAM-ID. LTVCHK01.
      *****************************************************************
      * LTV CHECK. LISTS EVERY ACTIVE LOAN WHOSE LOAN-TO-VALUE IS     *
      * ABOVE THE LIMIT (85%) FOR CREDIT RISK TO REVIEW.              *
      * RUN BY JOB LTVCHK.                                            *
      *****************************************************************
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT REPORT-FILE ASSIGN TO REPORT
               ORGANIZATION IS SEQUENTIAL.
       DATA DIVISION.
       FILE SECTION.
       FD  REPORT-FILE RECORDING MODE F.
       01  REPORT-LINE                 PIC X(80).
       WORKING-STORAGE SECTION.
       01  WS-MAX-LTV                  PIC S9V9(4) COMP-3 VALUE 0.8500.
       01  WS-COUNT                    PIC 9(7) VALUE 0.
       01  WS-EOF                      PIC X VALUE 'N'.
           88  NO-MORE-LOANS           VALUE 'Y'.
       01  WS-LINE.
           05  WL-ACCT                 PIC X(10).
           05  FILLER                  PIC X(2) VALUE SPACES.
           05  WL-CUSTOMER             PIC X(12).
           05  FILLER                  PIC X(2) VALUE SPACES.
           05  WL-LTV                  PIC 9.9999.
           05  FILLER                  PIC X(48) VALUE SPACES.
       COPY CUSTMAST.
       COPY LOANREC.
           EXEC SQL INCLUDE SQLCA END-EXEC.
           EXEC SQL DECLARE HIGHLTV CURSOR FOR
               SELECT LOAN_ID, CUSTOMER_ID, LTV_RATIO
                 FROM LOAN
                WHERE STATUS IN ('ACTIVE', 'DELINQUENT')
                  AND LTV_RATIO > :WS-MAX-LTV
           END-EXEC.
       PROCEDURE DIVISION.
       MAIN-PARA.
           OPEN OUTPUT REPORT-FILE
           EXEC SQL OPEN HIGHLTV END-EXEC
           PERFORM FETCH-LOAN
           PERFORM REPORT-ONE UNTIL NO-MORE-LOANS
           EXEC SQL CLOSE HIGHLTV END-EXEC
           CLOSE REPORT-FILE
           GOBACK.

       FETCH-LOAN.
           EXEC SQL FETCH HIGHLTV
               INTO :ACCT-NO, :LN-CUSTOMER-ID, :LN-LTV-RATIO
           END-EXEC
           IF SQLCODE = 100
               SET NO-MORE-LOANS TO TRUE
           END-IF.

       REPORT-ONE.
           MOVE LN-CUSTOMER-ID TO CM-CUSTOMER-ID
           MOVE ACCT-NO TO WL-ACCT
           MOVE CM-CUSTOMER-ID TO WL-CUSTOMER
           MOVE LN-LTV-RATIO TO WL-LTV
           WRITE REPORT-LINE FROM WS-LINE
           ADD 1 TO WS-COUNT
           PERFORM FETCH-LOAN.
