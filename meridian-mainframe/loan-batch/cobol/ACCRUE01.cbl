       IDENTIFICATION DIVISION.
       PROGRAM-ID. ACCRUE01.
      *****************************************************************
      * NIGHTLY INTEREST ACCRUAL.                                     *
      * FOR EVERY ACTIVE LOAN: ONE DAY OF INTEREST IS ADDED TO        *
      * LOAN.ACCRUED_INTEREST, AND THE LOAN IS WRITTEN TO THE ACCRUAL *
      * EXTRACT (DD ACCROUT) FOR STMTGEN1.                            *
      * THE INTEREST PROGRAM IS CALLED DYNAMICALLY THROUGH WS-INT-PGM.*
      *****************************************************************
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT ACCROUT ASSIGN TO ACCROUT
               ORGANIZATION IS SEQUENTIAL.
       DATA DIVISION.
       FILE SECTION.
       FD  ACCROUT RECORDING MODE F.
       01  ACCROUT-REC                 PIC X(150).
       WORKING-STORAGE SECTION.
       01  WS-INT-PGM                  PIC X(8) VALUE 'INTCALC1'.
       01  WS-BUSINESS-DATE            PIC 9(8).
       01  WS-PREV-DATE                PIC 9(8).
       01  WS-DAYS                     PIC S9(5) COMP-3.
       01  WS-INTEREST                 PIC S9(10)V99 COMP-3.
       01  WS-EOF                      PIC X VALUE 'N'.
           88  NO-MORE-LOANS           VALUE 'Y'.
       01  WS-DATE-PARMS.
           05  DU-FUNCTION             PIC X(4).
           05  DU-DATE-1               PIC 9(8).
           05  DU-DATE-2               PIC 9(8).
           05  DU-DAYS                 PIC S9(5) COMP-3.
       COPY CUSTMAST.
       COPY LOANREC.
           EXEC SQL INCLUDE SQLCA END-EXEC.
           EXEC SQL DECLARE LOANCSR CURSOR FOR
               SELECT L.LOAN_ID, L.CUSTOMER_ID, L.PRINCIPAL,
                      L.OUTSTANDING, L.ANNUAL_RATE, L.ACCRUED_INTEREST,
                      C.FULL_NAME
                 FROM LOAN L, CUSTOMER C
                WHERE L.CUSTOMER_ID = C.CUSTOMER_ID
                  AND L.STATUS IN ('ACTIVE', 'DELINQUENT')
               FOR UPDATE OF ACCRUED_INTEREST
           END-EXEC.
       PROCEDURE DIVISION.
       MAIN-PARA.
           ACCEPT WS-BUSINESS-DATE FROM DATE YYYYMMDD
           PERFORM GET-PREVIOUS-DATE
           OPEN OUTPUT ACCROUT
           EXEC SQL OPEN LOANCSR END-EXEC
           PERFORM FETCH-LOAN
           PERFORM ACCRUE-ONE-LOAN UNTIL NO-MORE-LOANS
           EXEC SQL CLOSE LOANCSR END-EXEC
           EXEC SQL COMMIT END-EXEC
           CLOSE ACCROUT
           GOBACK.

       GET-PREVIOUS-DATE.
      *    DAYS SINCE THE LAST RUN, COUNTED 30/360 BY DATEUTL1.
           MOVE 'PREV' TO DU-FUNCTION
           MOVE WS-BUSINESS-DATE TO DU-DATE-2
           CALL 'DATEUTL1' USING WS-DATE-PARMS
           MOVE DU-DATE-1 TO WS-PREV-DATE
           MOVE 'D360' TO DU-FUNCTION
           CALL 'DATEUTL1' USING WS-DATE-PARMS
           MOVE DU-DAYS TO WS-DAYS.

       FETCH-LOAN.
           EXEC SQL FETCH LOANCSR
               INTO :ACCT-NO, :LN-CUSTOMER-ID, :LN-PRINCIPAL,
                    :LN-OUTSTANDING, :LN-ANNUAL-RATE, :LN-ACCRUED-INT,
                    :CM-FULL-NAME
           END-EXEC
           IF SQLCODE = 100
               SET NO-MORE-LOANS TO TRUE
           END-IF.

       ACCRUE-ONE-LOAN.
           CALL WS-INT-PGM USING LN-OUTSTANDING LN-ANNUAL-RATE
                                 WS-DAYS WS-INTEREST
           ADD WS-INTEREST TO LN-ACCRUED-INT
           EXEC SQL UPDATE LOAN
                       SET ACCRUED_INTEREST = :LN-ACCRUED-INT
                     WHERE CURRENT OF LOANCSR
           END-EXEC
           MOVE LN-CUSTOMER-ID TO CM-CUSTOMER-ID
           WRITE ACCROUT-REC FROM LOAN-RECORD
           PERFORM FETCH-LOAN.
