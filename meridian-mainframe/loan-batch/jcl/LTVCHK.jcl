//LTVCHK   JOB (LEND),'LENDING LTV CHECK',CLASS=A,MSGCLASS=X
//*****************************************************************
//* WEEKLY LTV CHECK FOR CREDIT RISK. RUNS SUNDAYS 04:00.         *
//*****************************************************************
//STEP010  EXEC PGM=LTVCHK01
//STEPLIB  DD DSN=MERIDIAN.LENDING.LOADLIB,DISP=SHR
//REPORT   DD SYSOUT=R
//SYSOUT   DD SYSOUT=*
