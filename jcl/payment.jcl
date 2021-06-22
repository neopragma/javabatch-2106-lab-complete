//PAYMENT   JOB [...]
//*------------------------------------------------------------
//* Process payment files from external partners
//* 
//* Convert file from payment source 1 to our internal format
//*------------------------------------------------------------
//SRC1INP  EXEC PGM=ICEMAN,REGION=[...]
//STEPLIB    DD [...]
//SORTIN     DD DSN=SOURCE1.PAYMENTS, 
//              DISP=SHR 
//SORTOUT    DD DSN=&&SRC1FMT,
//              DISP=(NEW,PASS)
//SYSIN      DD * 
  OPTION COPY							
  INCLUDE COND=(91,8,CH,GE,DATE1-90)		
  INREC IFTHEN=(WHEN=(20,1,CH,EQ'N'),OVERLAY(21:21,2,C'42')),
        IFTHEN=(WHEN=(20,1,CH,EQ'S'),OVERLAY(21:21,2,C'64'))
  OUTREC FIELDS=(21,17,49,13,91,8,99,8,65,18,119,7,UFF,M11,LENGTH=9)
//*------------------------------------------------------------
//* Sort the reformatted payment file from partner 1
//*------------------------------------------------------------
//SORT1    EXEC SORT, 
// COND=((0,EQ,SRC1INP),ONLY)
//DFSPARM    DD * 
  EQUAL,ABEND
//SORTIN     DD DSN=&&SRC1FMT,
//              DISP=(OLD,DELETE)
//SORTOUT    DD DSN=&&SRC1SRT,
//              DISP=(NEW,PASS)
//SYSIN      DD * 
  SORT FIELDS=(1,30,CH,A,39,8,CH,A)
//*------------------------------------------------------------
//* Convert file from payment source 2 to our internal format 
//*------------------------------------------------------------
//SRC2INP  EXEC PGM=PMT2CNV,[...]
//SRC2PMT    DD DSN=SOURCE2.PAYMENTS, 
//              DISP=SHR 
//PMTSTD     DD DSN=&&SRC2FMT, 
//              DISP=(NEW,PASS)
//*------------------------------------------------------------
//* Sort the reformatted payment file from partner 2
//*------------------------------------------------------------
//SORT2    EXEC SORT, 
// COND=((0,EQ,SRC2INP),ONLY)
//DFSPARM    DD * 
  EQUAL,ABEND
//SORTIN     DD DSN=&&SRC2FMT,
//              DISP=(OLD,DELETE)
//SORTOUT    DD DSN=&&SRC2SRT,
//              DISP=(NEW,PASS)
//SYSIN      DD * 
  SORT FIELDS=(1,30,CH,A,39,8,CH,A)
//*------------------------------------------------------------
//* Merge the payment files from all external partners
//*------------------------------------------------------------
//MERGE    EXEC SORT, 
// COND=((0,EQ,SORT1),(0,EQ,SORT2),ONLY)
//DFSPARM    DD * 
  EQUAL,ABEND
//SORTIN01   DD DSN=&&SRC1SRT,
//              DISP=(OLD,DELETE)
//SORTIN02   DD DSN=&&SRC2SRT,
//              DISP=(OLD,DELETE)
//SORTOUT    DD DSN=&&PMTIN,
//              DISP=(NEW,CATLG,DELETE)
//SYSIN      DD * 
  MERGE FIELDS=(1,30,CH,A,39,8,CH,A)
//*------------------------------------------------------------
//* Process the payment data
//*------------------------------------------------------------
//STEP001  EXEC PGM=IKJEFT01,
// COND=((0,EQ,MERGE),ONLY)
//STEPLIB  DD DSN=THING.IBMMF.DBRMLIB,DISP=SHR
//*
//PMTIN      DD DSN=&&PMTIN,
//              DISP=(OLD,DELETE,KEEP) 
//PMTSUM     DD DSN=THING.PAYMENT.SUMMARY(+1), 
//              DISP=(NEW,CATLG,DELETE)
//PMTERR     DD DSN=THING.PAYMENT.ERRORS(+1), 
//              DISP=(NEW,CATLG,DELETE)
//SYSPRINT DD SYSOUT=*
//SYSABOUT DD SYSOUT=*
//SYSDBOUT DD SYSOUT=*
//SYSUDUMP DD SYSOUT=*
//DISPLAY  DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//SYSTSPRT DD SYSOUT=*
//SYSTSIN  DD *
    DSN SYSTEM(SSID)
    RUN PROGRAM(PMTPROG) PLAN(PLANNAME) -
    LIB('THING.IBMMF.LOADLIB')
    END
/*
