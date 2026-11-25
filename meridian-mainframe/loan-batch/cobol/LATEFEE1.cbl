       IDENTIFICATION DIVISION.
       PROGRAM-ID. LATEFEE1.
      *****************************************************************
      * LATE FEES. AN INSTALLMENT UNPAID MORE THAN WS-GRACE-DAYS      *
      * AFTER ITS DUE DATE IS CHARGED 5% OF THE INSTALLMENT, AT LEAST *
      * 15.00. THE FEE IS ADDED TO LOAN.OUTSTANDING.                  *
      * WS-GRACE-DAYS DEFAULTS TO 14; A GRACE-DAYS= LINE IN THE       *
      * PARAMETER FILE (DD PARMIN) OVERRIDES IT.                      *
      *****************************************************************
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT PARMIN ASSIGN TO PARMIN
               ORGANIZATION IS SEQUENTIAL
               FILE STATUS IS WS-PARM-STATUS.
       DATA DIVISION.
       FILE SECTION.
       FD  PARMIN RECORDING MODE F.
       01  PARM-LINE                   PIC X(80).
       WORKING-STORAGE SECTION.
       01  WS-GRACE-DAYS               PIC 9(3) VALUE 14.
       01  WS-FEE-RATE                 PIC V99  VALUE .05.
       01  WS-MIN-FEE                  PIC 9(3)V99 VALUE 15.00.
       01  WS-FEE                      PIC S9(7)V99 COMP-3.
       01  WS-PARM-STATUS              PIC XX.
       01  WS-PARM-EOF                 PIC X VALUE 'N'.
       01  WS-TODAY                    PIC 9(8).
       01  WS-DUE-DATE                 PIC 9(8).
       01  WS-INSTALLMENT-NO           PIC S9(4) COMP.
       01  WS-AMOUNT-DUE               PIC S9(10)V99 COMP-3.
       01  WS-DAYS-LATE                PIC S9(5) COMP-3.
       01  WS-EOF                      PIC X VALUE 'N'.
           88  NO-MORE-OVERDUE         VALUE 'Y'.
       01  WS-DATE-PARMS.
           05  DU-FUNCTION             PIC X(4).
           05  DU-DATE-1               PIC 9(8).
           05  DU-DATE-2               PIC 9(8).
           05  DU-DAYS                 PIC S9(5) COMP-3.
       COPY CUSTMAST.
       COPY LOANREC.
           EXEC SQL INCLUDE SQLCA END-EXEC.
           EXEC SQL DECLARE OVERDUE CURSOR FOR
               SELECT S.LOAN_ID, S.INSTALLMENT_NO, S.DUE_DATE,
                      S.AMOUNT_DUE, L.CUSTOMER_ID
                 FROM REPAYMENT_SCHEDULE S, LOAN L
                WHERE S.LOAN_ID = L.LOAN_ID
                  AND S.PAID = 'N'
                  AND S.DUE_DATE < CURRENT DATE
           END-EXEC.
       PROCEDURE DIVISION.
       MAIN-PARA.
           PERFORM READ-PARAMETERS
           ACCEPT WS-TODAY FROM DATE YYYYMMDD
           EXEC SQL OPEN OVERDUE END-EXEC
           PERFORM FETCH-OVERDUE
           PERFORM CHARGE-ONE UNTIL NO-MORE-OVERDUE
           EXEC SQL CLOSE OVERDUE END-EXEC
           EXEC SQL COMMIT END-EXEC
           GOBACK.

       READ-PARAMETERS.
           OPEN INPUT PARMIN
           IF WS-PARM-STATUS = '00'
               PERFORM UNTIL WS-PARM-EOF = 'Y'
                   READ PARMIN
                       AT END MOVE 'Y' TO WS-PARM-EOF
                       NOT AT END
                           IF PARM-LINE(1:11) = 'GRACE-DAYS='
                               MOVE PARM-LINE(12:3) TO WS-GRACE-DAYS
                           END-IF
                   END-READ
               END-PERFORM
               CLOSE PARMIN
           END-IF.

       FETCH-OVERDUE.
           EXEC SQL FETCH OVERDUE
               INTO :ACCT-NO, :WS-INSTALLMENT-NO, :WS-DUE-DATE,
                    :WS-AMOUNT-DUE, :LN-CUSTOMER-ID
           END-EXEC
           IF SQLCODE = 100
               SET NO-MORE-OVERDUE TO TRUE
           END-IF.

       CHARGE-ONE.
           MOVE 'DIFF' TO DU-FUNCTION
           MOVE WS-DUE-DATE TO DU-DATE-1
           MOVE WS-TODAY TO DU-DATE-2
           CALL 'DATEUTL1' USING WS-DATE-PARMS
           MOVE DU-DAYS TO WS-DAYS-LATE
           IF WS-DAYS-LATE > WS-GRACE-DAYS
               PERFORM CALC-FEE
               EXEC SQL UPDATE LOAN
                           SET OUTSTANDING = OUTSTANDING + :WS-FEE
                         WHERE LOAN_ID = :ACCT-NO
               END-EXEC
           END-IF
           PERFORM FETCH-OVERDUE.

       CALC-FEE.
           COMPUTE WS-FEE ROUNDED = WS-AMOUNT-DUE * WS-FEE-RATE
           IF WS-FEE < WS-MIN-FEE
               MOVE WS-MIN-FEE TO WS-FEE
           END-IF.

       OLD-FEE-CALC.
      *    FLAT 25.00 FEE, REPLACED BY CALC-FEE IN 2019.
           MOVE 25.00 TO WS-FEE.
