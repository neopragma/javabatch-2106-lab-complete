       IDENTIFICATION DIVISION. 
       Program-ID. PMTPROG.
       ENVIRONMENT DIVISION. 
       INPUT-OUTPUT SECTION. 
       FILE-CONTROL. 
           SELECT PAYMENTS-IN 
               ASSIGN TO 'PMTIN'  
               ORGANIZATION LINE SEQUENTIAL 
               FILE STATUS PMTIN-STATUS. 
           SELECT PAYMENT-SUMMARY 
               ASSIGN TO 'PMTSUM'  
               ORGANIZATION LINE SEQUENTIAL 
               FILE STATUS PMTSUM-STATUS. 
           SELECT PAYMENT-ERRORS 
               ASSIGN TO 'PMTERR'  
               ORGANIZATION LINE SEQUENTIAL 
               FILE STATUS PMTERR-STATUS. 
       DATA DIVISION. 
       File Section.
       FD  PAYMENTS-IN 
           Recording Mode F 
           Data Record PAYMENTS-STANDARD-REC.
       01  PAYMENTS-STANDARD-REC.
           05  PMT-CUSTOMER-ID          PIC X(17).
           05  PMT-INVOICE-NUMBER       PIC X(13).
           05  PMT-DATE-DUE             PIC X(08). 
           05  PMT-DATE-PAID            PIC X(08). 
           05  PMT-AMOUNT-PAID          PIC 9(16)V9(02).
           05  PMT-TAX-PAID             PIC 9(07)V9(02).
       FD  PAYMENT-SUMMARY 
           Recording Mode F 
           Data Record PAY-SUMMARY-REC.
       01  PAY-SUMMARY-REC. 
           05  PAY-CUSTOMER-ID       PIC X(17). 
           05  PAY-INVOICE-NUMBER    PIC X(13). 
           05  PAY-AMOUNT-PAID       PIC S9(13)V99 COMP-3. 
           05  PAY-TAX-PAID          PIC S9(13)V99 COMP-3. 
           05  PAY-DATE-RECEIVED     PIC X(08).
           05  PAY-STATUS            PIC X. 
       FD  PAYMENT-ERRORS 
           Recording Mode F 
           Data Record PAY-ERROR-REC.
       01  PAY-ERROR-REC.    
           05  ERR-CUSTOMER-ID       PIC X(17). 
           05  ERR-INVOICE-NUMBER    PIC X(13). 
           05  ERR-DATE-RECEIVED     PIC X(8). 
           05  ERR-CODE              PIC X. 
               88  ERR-NO-SUCH-CUSTOMER    VALUE 'C'.
               88  ERR-NO-SUCH-INVOICE     VALUE 'I'.
           05  ERR-DESCRIPTION       PIC X(80). 
                       
       WORKING-STORAGE SECTION. 

      *    EXEC SQL 
      *         INCLUDE SQLCA 
      *     END-EXEC.

      * File: sqlca.cbl
       01 SQLCA SYNC.
           05 SQLCAID PIC X(8) VALUE "SQLCA   ".
           05 SQLCABC PIC S9(9) COMP-5 VALUE 136.
           05 SQLCODE PIC S9(9) COMP-5.
           05 SQLERRM PIC X.
           05 SQLERRP PIC X(8).
           05 SQLERRD OCCURS 6 TIMES PIC S9(9) COMP-5.
           05 SQLWARN.
               10 SQLWARN0 PIC X.
               10 SQLWARN1 PIC X.
               10 SQLWARN2 PIC X.
               10 SQLWARN3 PIC X.
               10 SQLWARN4 PIC X.
               10 SQLWARN5 PIC X.
               10 SQLWARN6 PIC X.
               10 SQLWARN7 PIC X.
               10 SQLWARN8 PIC X.
               10 SQLWARN9 PIC X.
               10 SQLWARNA PIC X.
           05 SQLSTATE PIC X(5).
      *
      *    EXEC SQL 
      *        DECLARE THING.CUSTOMER 
      *           ( CUSTID        CHAR(17)    NOT NULL,
      *             CUSTNAME      CHAR(256)   NOT NULL ) 
      *     END-EXEC.

      *     EXEC SQL 
      *         DECLARE THING.INVOICE 
      *           ( INVNUM		  CHAR(13)    NOT NULL, 
      *             CUSTID		  CHAR(17)    NOT NULL,
      *             DUEDATE		  DATE        NOT NULL,  
      *             LASTPAY 	  DATE        NOT NULL,  
      *             AMTDUE        DECIMAL     NOT NULL,
      *             AMTPAID       DECIMAL     NOT NULL,
      *             TAXPAID       DECIMAL     NOT NULL,
      *             STATUS		  CHAR(1)     NOT NULL )
      *     END-EXEC.        

      * Host variables

       01  CUSTOMER-INFO. 
           05  CUST-ID               PIC X(17).
           05  CUST-NAME             PIC X(256). 

       01  INVOICE-INFO. 
           05  INV-NUMBER            PIC X(13).
           05  INV-CUSTOMER-ID       PIC X(17).
           05  INV-DUE-DATE          PIC X(08). 
           05  INV-LAST-PAY-DATE     PIC X(08).
           05  INV-AMOUNT-DUE        PIC S9(13)V99 COMP-3.
           05  INV-AMOUNT-PAID       PIC S9(13)V99 COMP-3.
           05  INV-TAX-PAID          PIC S9(11)V99 COMP-3.
           05  INV-STATUS            PIC X. 
               88  INV-GOOD-STANDING     VALUE SPACE. 
               88  INV-LATE-30-DAYS      VALUE 'A'. 
               88  INV-LATE-60-DAYS      VALUE 'B'. 
               88  INV-LATE-90-DAYS      VALUE 'C'. 
               88  INV-PAID              VALUE 'P'. 
               88  INV-OVERPAID          VALUE 'O'. 

      * Other working storage fields

       01  WS-File-Status.
           05  PMTIN-STATUS          PIC X(02).
               88  PMTIN-OK              VALUE '00'.
               88  PMTIN-EOF             VALUE '10'.
               88  PMTIN-NOTFOUND        VALUE '35'.
           05  PMTSUM-STATUS         PIC X(02).        
               88  PMTSUM-OK             VALUE '00'.
           05  PMTERR-STATUS         PIC X(02).        
               88  PMTERR-OK             VALUE '00'.
       01  WS-Date-Manipulation.
           05  WS-Date-YYYYMMDD      PIC 9(8).
           05  WS-Date-Integer       PIC S9(9).
           05  WS-Next-Due-Date-Interval PIC S9(9) VALUE +30.
       01  WS-Error-Handling.    
           05  WS-Error-Message      PIC X(133).  
           05  Filler                PIC X Value space.
               88 NO-ERRORS-FOUND        Value space. 
               88 ERRORS-FOUND           Value 'X'.          

       PROCEDURE DIVISION.
           Perform 1000-Initialize
           Perform 2000-Process
           Perform 3000-Housekeeping
           Goback
           .
       1000-Initialize.
           PERFORM 1100-OPEN-PAYMENTS-IN 
           PERFORM 1200-OPEN-PAYMENT-SUMMARY 
           PERFORM 1300-OPEN-PAYMENT-ERRORS
           .
       1100-OPEN-PAYMENTS-IN.    
           OPEN INPUT PAYMENTS-IN
           EVALUATE TRUE
               WHEN PMTIN-OK
                   CONTINUE
               WHEN PMTIN-NOTFOUND
                   MOVE 'Input file not found'
                     TO WS-ERROR-MESSAGE
                     PERFORM 9999-ABORT
               WHEN OTHER
                   STRING "Unexpected PMTIN file status on open "
                       DELIMITED BY SIZE
                       PMTIN-STATUS
                       DELIMITED BY SIZE
                     INTO WS-ERROR-MESSAGE
                   PERFORM 9999-ABORT
           END-EVALUATE
           .
       1200-OPEN-PAYMENT-SUMMARY.    
           OPEN OUTPUT PAYMENT-SUMMARY
           EVALUATE TRUE
               WHEN PMTSUM-OK
                   CONTINUE
               WHEN OTHER
                   STRING "Unexpected PMTSUM file status on open "
                       DELIMITED BY SIZE
                       PMTSUM-STATUS
                       DELIMITED BY SIZE
                     INTO WS-ERROR-MESSAGE
                   PERFORM 9999-ABORT
           END-EVALUATE
           .
       1300-OPEN-PAYMENT-ERRORS.
           OPEN OUTPUT PAYMENT-ERRORS
           EVALUATE TRUE
               WHEN PMTERR-OK
                   CONTINUE
               WHEN OTHER
                   STRING "Unexpected PMTERR file status on open "
                       DELIMITED BY SIZE
                       PMTERR-STATUS
                       DELIMITED BY SIZE
                     INTO WS-ERROR-MESSAGE
                   PERFORM 9999-ABORT
           END-EVALUATE
           . 
       2000-Process. 
           SET NO-ERRORS-FOUND TO TRUE
           PERFORM 2100-READ-AND-PROCESS-PAYMENT
           PERFORM WITH TEST BEFORE
                   UNTIL PMTIN-EOF
               PERFORM 2100-READ-AND-PROCESS-PAYMENT
           END-PERFORM
           .    
       2100-READ-AND-PROCESS-PAYMENT.
           Read PAYMENTS-IN 
           EVALUATE TRUE 
               WHEN PMTIN-OK
                   PERFORM 2200-PROCESS-PAYMENT 
               WHEN PMTIN-EOF 
                   CONTINUE
               WHEN OTHER        
                   STRING "Unexpected PMTIN file status on read "
                       DELIMITED BY SIZE
                       PMTIN-STATUS
                       DELIMITED BY SIZE
                     INTO WS-ERROR-MESSAGE
                   PERFORM 9999-ABORT
           END-EVALUATE
           .      

       2200-PROCESS-PAYMENT.    
      * See if the customer is in the database 
      * EXEC SQL 
      *    SELECT CUSTID 
      *        INTO :CUST-ID 
      *    FROM CUSTOMER 
      *    WHERE CUSTID = PMT-CUSTOMER-ID 
      * END-EXEC         

      *=================================================================
           PERFORM XXXX-PRETEND-CUSTOMER-QUERY
      *=================================================================

           IF SQLCODE EQUAL ZERO 
               PERFORM 2600-CHECK-INVOICE
           ELSE     
               INITIALIZE PAY-ERROR-REC 
               SET ERR-NO-SUCH-CUSTOMER TO TRUE 
               PERFORM 2900-PAYMENT-ERROR 
           END-IF 
           .
       2600-CHECK-INVOICE.  
      *    EXEC SQL 
      *        SELECT   
      *            INVNUM, CUSTID, DUEDATE, LASTPAY, AMTDUE, 
      *            AMTPAID, TAXPAID, STATUS 
      *        INTO 
      *            :INV-NUMBER, 
      *            :INV-CUSTOMER-ID,
      *            :INV-DUE-DATE, 
      *            :INV-LAST-PAY-DATE,
      *            :INV-AMOUNT-DUE, 
      *            :INV-AMOUNT-PAID, 
      *            :INV-TAX-PAID, 
      *            :INV-STATUS 
      *        FROM THING.INVOICE 
      *        WHERE INVNUM = PMT-INVOICE-NUMBER 
      *        AND CUSTID = PMT-CUSTOMER-ID 
      *    END-EXEC 

      *=================================================================
           PERFORM XXXX-PRETEND-INVOICE-QUERY
      *=================================================================

           IF SQLCODE EQUAL ZERO 
               PERFORM 2700-APPLY-PAYMENT
           ELSE     
               INITIALIZE PAY-ERROR-REC 
               SET ERR-NO-SUCH-INVOICE TO TRUE 
               PERFORM 2900-PAYMENT-ERROR 
           END-IF 
           .
       2700-APPLY-PAYMENT.
           ADD PMT-AMOUNT-PAID TO INV-AMOUNT-PAID
           ADD PMT-TAX-PAID TO INV-TAX-PAID

           MOVE FUNCTION CURRENT-DATE(1:8) TO WS-Date-YYYYMMDD 
           MOVE WS-Date-YYYYMMDD TO INV-LAST-PAY-DATE

           ADD WS-Next-Due-Date-Interval TO WS-Date-Integer 
           COMPUTE WS-Date-YYYYMMDD = 
               FUNCTION DATE-OF-INTEGER(WS-Date-Integer) 
           END-COMPUTE   
           MOVE WS-Date-YYYYMMDD TO INV-DUE-DATE      

           EVALUATE TRUE 
               WHEN INV-AMOUNT-PAID GREATER INV-AMOUNT-DUE 
                   SET INV-OVERPAID TO TRUE 
               WHEN INV-AMOUNT-PAID EQUAL INV-AMOUNT-DUE 
                   SET INV-PAID TO TRUE 
               WHEN OTHER 
                   SET INV-GOOD-STANDING TO TRUE 
           END-EVALUATE                

      *    EXEC SQL 
      *        UPDATE THING.INVOICE 
      *            SET LASTPAY = TO_DATE(:INV-LAST-PAY-DATE, "%Y%m%d") 
      *            SET DUEDATE = TO_DATE(:INV-DUE-DATE, "%Y%m%d") 
      *            SET AMTPAID = :INV-AMOUNT-PAID 
      *            SET TAXPAID = :INV-TAX-PAID 
      *            SET STATUS = :INV-STATUS
      *        WHERE INVNUM = PMT-INVOICE-NUMBER 
      *        AND CUSTID = PMT-CUSTOMER-ID 
      *    END-EXEC 

      *=================================================================
           PERFORM XXXX-PRETEND-INVOICE-UPDATE
      *=================================================================
           .

       2900-PAYMENT-ERROR.
           EVALUATE TRUE
               WHEN ERR-NO-SUCH-CUSTOMER
                  STRING "Customer " DELIMITED BY SIZE 
                         PMT-CUSTOMER-ID DELIMITED BY SIZE 
                         " is not registered" DELIMITED BY SIZE
                      INTO ERR-DESCRIPTION
                  END-STRING  
               WHEN ERR-NO-SUCH-INVOICE  
                  STRING "No invoice " DELIMITED BY SIZE 
                        PMT-INVOICE-NUMBER DELIMITED BY SIZE 
                        " for customer " DELIMITED BY SIZE 
                        PMT-CUSTOMER-ID DELIMITED BY SIZE 
                        " was found" DELIMITED BY SIZE  
                      INTO ERR-DESCRIPTION  
               WHEN OTHER
                  STRING "Unexpected error encountered processing "
                          DELIMITED BY SIZE 
                        " invoice number " DELIMITED BY SIZE 
                        PMT-INVOICE-NUMBER DELIMITED By SIZE 
                        " for customer " DELIMITED BY SIZE 
                        PMT-CUSTOMER-ID DELIMITED BY SIZE 
                      INTO ERR-DESCRIPTION      
           END-EVALUATE
           PERFORM 2950-WRITE-PAYMENT-ERROR
           . 
       2950-WRITE-PAYMENT-ERROR.
           WRITE PAY-ERROR-REC 
           IF PMTERR-OK 
               CONTINUE 
           ELSE  
               STRING "Unexpected PMTERR file status on write "
                   DELIMITED BY SIZE
                   PMTERR-STATUS
                   DELIMITED BY SIZE
                 INTO WS-ERROR-MESSAGE
               PERFORM 9999-ABORT
           END-IF    
           .    

       3000-Housekeeping. 
           Close PAYMENTS-IN 
           Close PAYMENT-SUMMARY 
           Close PAYMENT-ERRORS
           .    
       9999-Abort.
           Set ERRORS-FOUND to TRUE
           Display WS-ERROR-MESSAGE
           GOBACK 
           .

       XXXX-PRETEND-CUSTOMER-QUERY.
           DISPLAY SPACE
           DISPLAY "Pretending to query customer " PMT-CUSTOMER-ID 
           IF PMT-CUSTOMER-ID(3:1) EQUAL "1" 
               MOVE 100 TO SQLCODE  
           ELSE  
               MOVE 0 TO SQLCODE    
           END-IF     
           DISPLAY "    Setting SQLCODE to " SQLCODE 
           .

       XXXX-PRETEND-INVOICE-QUERY.
           DISPLAY SPACE
           DISPLAY "Pretending to query invoice " PMT-INVOICE-NUMBER  
                   " for customer " PMT-CUSTOMER-ID
           IF PMT-CUSTOMER-ID(3:1) EQUAL "5" 
               MOVE 100 TO SQLCODE  
           ELSE  
               MOVE 0 TO SQLCODE 
               MOVE PMT-CUSTOMER-ID TO INV-CUSTOMER-ID
               MOVE PMT-INVOICE-NUMBER TO INV-NUMBER 
               MOVE 45.67 TO INV-AMOUNT-PAID 
               MOVE 1000.04 TO INV-AMOUNT-DUE 
               MOVE 8.99 TO INV-TAX-PAID 

           END-IF  
           DISPLAY "    Setting SQLCODE to " SQLCODE    
           .

       XXXX-PRETEND-INVOICE-UPDATE.
           DISPLAY SPACE
           DISPLAY "Pretending to update table THING.INVOICE:"
           DISPLAY "    CUSTID = " INV-CUSTOMER-ID 
           DISPLAY "    INVNUM = " INV-NUMBER 
           DISPLAY "    LASTPAY = " INV-LAST-PAY-DATE  
           DISPLAY "    DUEDATE = " INV-DUE-DATE  
           DISPLAY "    AMTPAID = " INV-AMOUNT-PAID  
           DISPLAY "    TAXPAID = " INV-TAX-PAID 
           DISPLAY "    STATUS = " INV-STATUS 
           .