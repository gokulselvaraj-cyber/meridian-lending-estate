       IDENTIFICATION DIVISION.
       PROGRAM-ID. DATEUTL1.
      *****************************************************************
      * DATE ARITHMETIC FOR THE LENDING BATCH.                        *
      *   DU-FUNCTION 'D360': DAYS FROM DATE-1 TO DATE-2, 30/360      *
      *   DU-FUNCTION 'DIFF': ACTUAL DAYS FROM DATE-1 TO DATE-2       *
      *   DU-FUNCTION 'PREV': DATE-1 = THE BUSINESS DAY BEFORE DATE-2 *
      * CALLED BY ACCRUE01, LATEFEE1, STMTGEN1.                       *
      *****************************************************************
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-D1.
           05  WS-Y1                   PIC 9(4).
           05  WS-M1                   PIC 9(2).
           05  WS-DD1                  PIC 9(2).
       01  WS-D2.
           05  WS-Y2                   PIC 9(4).
           05  WS-M2                   PIC 9(2).
           05  WS-DD2                  PIC 9(2).
       01  WS-INT-1                    PIC S9(9) COMP.
       01  WS-INT-2                    PIC S9(9) COMP.
       LINKAGE SECTION.
       01  LK-PARMS.
           05  DU-FUNCTION             PIC X(4).
           05  DU-DATE-1               PIC 9(8).
           05  DU-DATE-2               PIC 9(8).
           05  DU-DAYS                 PIC S9(5) COMP-3.
       PROCEDURE DIVISION USING LK-PARMS.
       MAIN-PARA.
           EVALUATE DU-FUNCTION
               WHEN 'D360' PERFORM DAYS-30-360
               WHEN 'DIFF' PERFORM DAYS-ACTUAL
               WHEN 'PREV' PERFORM PREVIOUS-DAY
           END-EVALUATE
           GOBACK.

       DAYS-30-360.
           MOVE DU-DATE-1 TO WS-D1
           MOVE DU-DATE-2 TO WS-D2
           IF WS-DD1 = 31 MOVE 30 TO WS-DD1 END-IF
           IF WS-DD2 = 31 MOVE 30 TO WS-DD2 END-IF
           COMPUTE DU-DAYS = (WS-Y2 - WS-Y1) * 360
                           + (WS-M2 - WS-M1) * 30
                           + (WS-DD2 - WS-DD1).

       DAYS-ACTUAL.
           COMPUTE WS-INT-1 = FUNCTION INTEGER-OF-DATE(DU-DATE-1)
           COMPUTE WS-INT-2 = FUNCTION INTEGER-OF-DATE(DU-DATE-2)
           COMPUTE DU-DAYS = WS-INT-2 - WS-INT-1.

       PREVIOUS-DAY.
           COMPUTE WS-INT-2 = FUNCTION INTEGER-OF-DATE(DU-DATE-2) - 1
           COMPUTE DU-DATE-1 = FUNCTION DATE-OF-INTEGER(WS-INT-2).
