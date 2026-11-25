      *****************************************************************
      * CUSTMAST - CUSTOMER MASTER RECORD (TABLE CUSTOMER)            *
      * ONE ROW PER BORROWER. CM-NATIONAL-ID AND CM-DOB ARE PERSONAL  *
      * DATA: NEVER DISPLAY OR WRITE THEM TO SYSOUT.                  *
      *****************************************************************
       01  CUSTMAST-RECORD.
           05  CM-CUSTOMER-ID          PIC X(12).
           05  CM-FULL-NAME            PIC X(60).
           05  CM-NATIONAL-ID          PIC X(20).
           05  CM-DOB                  PIC 9(8).
           05  CM-POSTCODE             PIC X(10).
           05  CM-EMAIL                PIC X(60).
