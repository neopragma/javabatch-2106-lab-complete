       IDENTIFICATION DIVISION. 
       Program-ID. PMT2CNV.
       ENVIRONMENT DIVISION. 
       INPUT-OUTPUT SECTION. 
       FILE-CONTROL. 
           SELECT SRC2-PAYMENTS-IN 
               ASSIGN TO 'SRC2PMT'
               ORGANIZATION LINE SEQUENTIAL 
               ACCESS MODE SEQUENTIAL 
               FILE STATUS SRC2PMT-IN-STATUS. 
           SELECT PAYMENTS-STANDARD 
               ASSIGN TO 'PMTSTD'  
               ORGANIZATION LINE SEQUENTIAL 
               FILE STATUS PMTSTD-STATUS. 
       DATA DIVISION. 
       File Section.
       FD  SRC2-PAYMENTS-IN
           Recording Mode V 
           Record Varying from 1 to 120
           Data Record SRC2PMT-REC.
       01  SRC2PMT-REC.
           05  Filler                  Pic X(120).
       FD  PAYMENTS-STANDARD 
           Recording Mode F 
           Data Record PAYMENTS-STANDARD-REC.
       01  PAYMENTS-STANDARD-REC.
           05  PMT-CUSTOMER-ID          PIC X(17).
           05  PMT-INVOICE-NUMBER       PIC X(13).
           05  PMT-DATE-DUE             PIC X(08). 
           05  PMT-DATE-PAID            PIC X(08). 
           05  PMT-AMOUNT-PAID          PIC 9(16)V9(02).
           05  PMT-TAX-PAID             PIC 9(07)V9(02).
       WORKING-STORAGE SECTION. 
       01  WS-File-Status.
           05  SRC2PMT-IN-STATUS      PIC X(02).
               88  SRC2PMT-IN-OK       VALUE '00'.
               88  SRC2PMT-IN-EOF      VALUE '10'.
               88  SRC2PMT-IN-NOTFOUND VALUE '35'.
           05  PMTSTD-STATUS       PIC X(02).        
               88  PMTSTD-OK           VALUE '00'.
       01  WS-Date-Format-Fields. 
           05  WS-Date-Paid. 
               10  WS-Year         PIC X(04).
               10  FILLER          PIC X.
               10  WS-Month        PIC X(02).
               10  FILLER          PIC X.
               10  WS-Day          PIC X(02).        
           05  WS-Date-Due. 
               10  WS-Year         PIC X(04).
               10  FILLER          PIC X.
               10  WS-Month        PIC X(02).
               10  FILLER          PIC X.
               10  WS-Day          PIC X(02).  
           05  WS-Formatted-Date.
               10  WS-Year         PIC X(04).
               10  WS-Month        PIC X(02).
               10  WS-Day          PIC X(02).    

       01  WS-General.
           05  WS-Error-Message    PIC X(133).  
           05  Filler              PIC X Value space.
               88 NO-ERRORS-FOUND        Value space. 
               88 ERRORS-FOUND           Value 'X'.          
       PROCEDURE DIVISION.
           Perform 1000-Initialize
           Perform 2000-Convert
           Perform 3000-Housekeeping
           Goback
           .
       1000-Initialize.
           OPEN INPUT SRC2-PAYMENTS-IN
           EVALUATE TRUE
               WHEN SRC2PMT-IN-OK
                   CONTINUE
               WHEN SRC2PMT-IN-NOTFOUND
                   MOVE 'Input file not found'
                     TO WS-ERROR-MESSAGE
                     PERFORM 9999-ABORT
               WHEN OTHER
                   STRING "Unexpected input file status on open "
                       DELIMITED BY SIZE
                       SRC2PMT-IN-STATUS
                       DELIMITED BY SIZE
                     INTO WS-ERROR-MESSAGE
                   PERFORM 9999-ABORT
           END-EVALUATE

           OPEN OUTPUT PAYMENTS-STANDARD
           EVALUATE TRUE
               WHEN PMTSTD-OK
                   CONTINUE
               WHEN OTHER
                   STRING "Unexpected output file status on open "
                       DELIMITED BY SIZE
                       PMTSTD-STATUS
                       DELIMITED BY SIZE
                     INTO WS-ERROR-MESSAGE
                   PERFORM 9999-ABORT
           END-EVALUATE
           . 
       2000-Convert. 
           SET NO-ERRORS-FOUND TO TRUE
           READ SRC2-PAYMENTS-IN
      * Skip the header record     
           READ SRC2-PAYMENTS-IN
           PERFORM WITH TEST BEFORE
                   UNTIL SRC2PMT-IN-EOF
               PERFORM 2200-CONVERT-AND-WRITE
               READ SRC2-PAYMENTS-IN
           END-PERFORM
           .
           
       2200-CONVERT-AND-WRITE.
           UNSTRING SRC2PMT-REC 
               DELIMITED BY "," 
               INTO 
                   PMT-CUSTOMER-ID
                   PMT-INVOICE-NUMBER 
                   PMT-AMOUNT-PAID    
                   PMT-TAX-PAID  
                   WS-Date-Paid       
                   WS-Date-Due   
           END-UNSTRING    
           MOVE CORRESPONDING WS-Date-Paid TO WS-Formatted-Date  
           MOVE WS-Formatted-Date  TO PMT-DATE-PAID
           MOVE CORRESPONDING WS-Date-Paid TO WS-Formatted-Date 
           MOVE WS-Formatted-Date  TO PMT-DATE-DUE
           WRITE PAYMENTS-STANDARD-REC
           .    
       3000-Housekeeping. 
           Close SRC2-PAYMENTS-IN 
           Close PAYMENTS-STANDARD
           .    
       9999-Abort.
           Display WS-ERROR-MESSAGE
           GOBACK 
           .