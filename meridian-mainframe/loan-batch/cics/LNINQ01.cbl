       IDENTIFICATION DIVISION.
       PROGRAM-ID. LNINQ01.
      *****************************************************************
      * CICS LOAN ENQUIRY. TRANSACTION LNIQ.                          *
      * THE AGENT KEYS A LOAN ACCOUNT NUMBER ON 3270 MAP LNINQM; THE  *
      * PROGRAM SHOWS ITS OUTSTANDING BALANCE, ACCRUED INTEREST AND   *
      * STATUS. READ ONLY: NOTHING IS CHANGED.                        *
      *****************************************************************
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  LNINQMI.
           05  FILLER                  PIC X(12).
           05  ACCTL                   PIC S9(4) COMP.
           05  ACCTF                   PIC X.
           05  ACCTI                   PIC X(10).
       01  LNINQMO.
           05  FILLER                  PIC X(12).
           05  FILLER                  PIC X(3).
           05  ACCTO                   PIC X(10).
           05  FILLER                  PIC X(3).
           05  BALO                    PIC Z,ZZZ,ZZZ,ZZ9.99.
           05  FILLER                  PIC X(3).
           05  INTO-ACCR               PIC Z,ZZZ,ZZZ,ZZ9.99.
           05  FILLER                  PIC X(3).
           05  STATO                   PIC X(12).
           05  FILLER                  PIC X(3).
           05  MSGO                    PIC X(60).
       COPY LOANREC.
           EXEC SQL INCLUDE SQLCA END-EXEC.
       PROCEDURE DIVISION.
       MAIN-PARA.
           EXEC CICS RECEIVE MAP('LNINQM') MAPSET('LNINQM')
                     INTO(LNINQMI) NOHANDLE
           END-EXEC
           MOVE ACCTI TO ACCT-NO
           EXEC SQL SELECT OUTSTANDING, ACCRUED_INTEREST, STATUS
                      INTO :LN-OUTSTANDING, :LN-ACCRUED-INT, :LN-STATUS
                      FROM LOAN
                     WHERE LOAN_ID = :ACCT-NO
           END-EXEC
           MOVE ACCT-NO TO ACCTO
           IF SQLCODE = 0
               MOVE LN-OUTSTANDING TO BALO
               MOVE LN-ACCRUED-INT TO INTO-ACCR
               MOVE LN-STATUS      TO STATO
               MOVE SPACES         TO MSGO
           ELSE
               MOVE 'LOAN ACCOUNT NOT FOUND' TO MSGO
           END-IF
           EXEC CICS SEND MAP('LNINQM') MAPSET('LNINQM')
                     FROM(LNINQMO) ERASE
           END-EXEC
           EXEC CICS RETURN TRANSID('LNIQ') END-EXEC.
