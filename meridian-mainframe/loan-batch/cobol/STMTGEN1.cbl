       IDENTIFICATION DIVISION.
       PROGRAM-ID. STMTGEN1.
      *****************************************************************
      * DAILY STATEMENTS. READS THE ACCRUAL EXTRACT WRITTEN BY        *
      * ACCRUE01 (DD ACCRIN) AND WRITES ONE 80-BYTE STATEMENT RECORD  *
      * PER LOAN TO DD STMTOUT (MERIDIAN.LENDING.STMT.DAILY):         *
      *   1-10 LOAN ID, 11-22 CUSTOMER ID, 23-35 OUTSTANDING,         *
      *   36-48 ACCRUED INTEREST (2 IMPLIED DECIMALS), 49-56 DATE     *
      *****************************************************************
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT ACCRIN  ASSIGN TO ACCRIN  ORGANIZATION IS SEQUENTIAL.
           SELECT STMTOUT ASSIGN TO STMTOUT ORGANIZATION IS SEQUENTIAL.
       DATA DIVISION.
       FILE SECTION.
       FD  ACCRIN RECORDING MODE F.
       01  ACCRIN-REC                  PIC X(150).
       FD  STMTOUT RECORDING MODE F.
       01  STMT-REC.
           05  ST-LOAN-ID              PIC X(10).
           05  ST-CUSTOMER-ID          PIC X(12).
           05  ST-OUTSTANDING          PIC 9(11)V99.
           05  ST-ACCRUED              PIC 9(11)V99.
           05  ST-DATE                 PIC 9(8).
           05  FILLER                  PIC X(24).
       WORKING-STORAGE SECTION.
       01  WS-EOF                      PIC X VALUE 'N'.
           88  END-OF-EXTRACT          VALUE 'Y'.
       01  WS-DATE-PARMS.
           05  DU-FUNCTION             PIC X(4).
           05  DU-DATE-1               PIC 9(8).
           05  DU-DATE-2               PIC 9(8).
           05  DU-DAYS                 PIC S9(5) COMP-3.
       COPY CUSTMAST.
       COPY LOANREC.
       PROCEDURE DIVISION.
       MAIN-PARA.
      *    THE STATEMENT DATE IS THE BUSINESS DAY THE ACCRUAL COVERED.
           ACCEPT DU-DATE-2 FROM DATE YYYYMMDD
           MOVE 'PREV' TO DU-FUNCTION
           CALL 'DATEUTL1' USING WS-DATE-PARMS
           OPEN INPUT ACCRIN OUTPUT STMTOUT
           PERFORM READ-EXTRACT
           PERFORM WRITE-STATEMENT UNTIL END-OF-EXTRACT
           CLOSE ACCRIN STMTOUT
           GOBACK.

       READ-EXTRACT.
           READ ACCRIN INTO LOAN-RECORD
               AT END SET END-OF-EXTRACT TO TRUE
           END-READ.

       WRITE-STATEMENT.
           MOVE LN-CUSTOMER-ID TO CM-CUSTOMER-ID
           MOVE ACCT-NO        TO ST-LOAN-ID
           MOVE CM-CUSTOMER-ID TO ST-CUSTOMER-ID
           MOVE LN-OUTSTANDING TO ST-OUTSTANDING
           MOVE LN-ACCRUED-INT TO ST-ACCRUED
           MOVE DU-DATE-1      TO ST-DATE
           WRITE STMT-REC
           PERFORM READ-EXTRACT.
