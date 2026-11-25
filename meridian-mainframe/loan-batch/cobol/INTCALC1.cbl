       IDENTIFICATION DIVISION.
       PROGRAM-ID. INTCALC1.
      *****************************************************************
      * INTEREST FOR A PERIOD. DAY COUNT 30/360: EVERY MONTH HAS 30   *
      * DAYS AND THE YEAR HAS 360.                                    *
      *   INTEREST = PRINCIPAL * RATE / 100 * DAYS / 360              *
      * CALLED BY ACCRUE01.                                           *
      *****************************************************************
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-DAYS-IN-YEAR             PIC 9(3) VALUE 360.
       LINKAGE SECTION.
       01  LK-PRINCIPAL                PIC S9(10)V99 COMP-3.
       01  LK-ANNUAL-RATE              PIC S9(3)V99  COMP-3.
       01  LK-DAYS                     PIC S9(5)     COMP-3.
       01  LK-INTEREST                 PIC S9(10)V99 COMP-3.
       PROCEDURE DIVISION USING LK-PRINCIPAL LK-ANNUAL-RATE
                                LK-DAYS LK-INTEREST.
       MAIN-PARA.
           COMPUTE LK-INTEREST ROUNDED =
               LK-PRINCIPAL * LK-ANNUAL-RATE / 100
                            * LK-DAYS / WS-DAYS-IN-YEAR
           GOBACK.
