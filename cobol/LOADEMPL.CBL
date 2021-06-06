       IDENTIFICATION DIVISION.
       PROGRAM-ID.  LOADEMPL.
      *****************************************************************
      * Load employee information.
      *****************************************************************       
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT EMPL-IN ASSIGN TO "EMPLIN"
               ORGANIZATION LINE SEQUENTIAL
               ACCESS MODE SEQUENTIAL
               FILE STATUS EMPL-IN-STATUS.
           SELECT EMPL-VALIDATED
               ASSIGN TO "EMPLVALD"
               ORGANIZATION LINE SEQUENTIAL
               ACCESS MODE SEQUENTIAL
               FILE STATUS IS EMPL-VALIDATED-STATUS.
           SELECT EMPL-ERRORS
               ASSIGN TO "EMPLERR"
               ORGANIZATION LINE SEQUENTIAL
               ACCESS MODE SEQUENTIAL
               FILE STATUS IS EMPL-ERRORS-STATUS.
       DATA DIVISION.
       FILE SECTION.
      * Raw employee data that has not been validated
       FD  EMPL-IN
           DATA RECORD IS EMPL-IN-REC.
       01  EMPL-IN-REC.
           COPY EMPLREC.
      * Employee records that have been validated
       FD  EMPL-VALIDATED
           DATA RECORD IS EMPL-VALIDATED-REC.
       01  EMPL-VALIDATED-REC.
           COPY EMPLREC.
      * Employee records with validation errors 
       FD  EMPL-ERRORS 
           DATA RECORD IS EMPL-ERRORS-REC. 
       01  EMPL-ERRORS-REC. 
           COPY EMPLREC. 
           05  EMPL-ERROR-MESSAGE    PIC X(50).         
       WORKING-STORAGE SECTION.
       01  FILLER.
           05  EMPL-IN-STATUS        PIC XX.
               88  EMPL-IN-OK        VALUE '00'.
               88  EMPL-IN-EOF       VALUE '10'.
               88  EMPL-IN-NOTFOUND  VALUE '35'.
           05  EMPL-VALIDATED-STATUS PIC XX.
               88  EMPL-VALD-OK      VALUE '00'.
           05  EMPL-ERRORS-STATUS PIC XX.
               88  EMPL-ERRORS-OK    VALUE '00'.
           05  WS-COUNTERS.    
               10  WS-VALID-COUNT        PIC S9(5) COMP-3.
               10  WS-ERROR-COUNT        PIC S9(5) COMP-3.
           05  WS-COUNT-FORMATTED PIC ZZ,ZZ9.
           05  WS-ERROR-MESSAGE   PIC X(50).
               88  NO-ERRORS-FOUND VALUE SPACES.
       01  WS-CURRENT-DATE-FIELDS.  
           05  WS-CURRENT-DATE.
               10  WS-CURRENT-YEAR    PIC  9(4).
               10  WS-CURRENT-MONTH   PIC  9(2).
               10  WS-CURRENT-DAY     PIC  9(2).
           05  WS-CURRENT-TIME.
               10  WS-CURRENT-HOUR    PIC  9(2).
               10  WS-CURRENT-MINUTE  PIC  9(2).
               10  WS-CURRENT-SECOND  PIC  9(2).
               10  WS-CURRENT-MS      PIC  9(2).
           05  WS-DIFF-FROM-GMT       PIC S9(4).               
       PROCEDURE DIVISION.
           PERFORM 1000-INITIALIZE
           PERFORM 5000-PROCESS
           PERFORM 8000-HOUSEKEEPING
           GOBACK
           .
       1000-INITIALIZE.
           OPEN INPUT EMPL-IN
           EVALUATE TRUE
               WHEN EMPL-IN-OK
                   CONTINUE
               WHEN EMPL-IN-NOTFOUND
                   MOVE 'Input file not found'
                     TO WS-ERROR-MESSAGE
                     PERFORM 9999-ABORT
               WHEN OTHER
                   STRING "Unexpected input file status on open "
                       DELIMITED BY SIZE
                       EMPL-IN-STATUS
                       DELIMITED BY SIZE
                     INTO WS-ERROR-MESSAGE
                   PERFORM 9999-ABORT
           END-EVALUATE

           OPEN OUTPUT EMPL-VALIDATED
           EVALUATE TRUE
               WHEN EMPL-VALD-OK
                   CONTINUE
               WHEN OTHER
                   STRING "Unexpected validated file status on open "
                       DELIMITED BY SIZE
                       EMPL-VALIDATED-STATUS
                       DELIMITED BY SIZE
                     INTO WS-ERROR-MESSAGE
                   PERFORM 9999-ABORT
           END-EVALUATE

           OPEN OUTPUT EMPL-ERRORS
           EVALUATE TRUE
               WHEN EMPL-ERRORS-OK
                   CONTINUE
               WHEN OTHER
                   STRING "Unexpected error file status on open "
                       DELIMITED BY SIZE
                       EMPL-ERRORS-STATUS
                       DELIMITED BY SIZE
                     INTO WS-ERROR-MESSAGE
                   PERFORM 9999-ABORT
           END-EVALUATE

           INITIALIZE WS-COUNTERS
           MOVE FUNCTION CURRENT-DATE TO WS-CURRENT-DATE-FIELDS
           .

       5000-PROCESS.
           SET NO-ERRORS-FOUND TO TRUE
           READ EMPL-IN
           PERFORM WITH TEST BEFORE
                   UNTIL EMPL-IN-EOF
               PERFORM 5200-VALIDATE-AND-WRITE
               READ EMPL-IN
           END-PERFORM
           .

       5200-VALIDATE-AND-WRITE.
           SET NO-ERRORS-FOUND TO TRUE
           PERFORM 5210-VALIDATE-SSN
           IF NO-ERRORS-FOUND 
               PERFORM 5220-VALIDATE-HIRE-DATE
           END-IF    
           IF NO-ERRORS-FOUND 
               PERFORM 5400-WRITE-VALIDATED-REC 
           ELSE        
               PERFORM 5600-WRITE-ERROR-REC 
           END-IF 
           .    

       5210-VALIDATE-SSN.
           EVALUATE TRUE
               WHEN TEST-SSN IN EMPL-IN 
                  CONTINUE
               WHEN EMPL-SSN IN EMPL-IN-REC(1:3) EQUAL "666"
                  MOVE "SSN area can't be 666" TO WS-ERROR-MESSAGE
               WHEN EMPL-SSN IN EMPL-IN-REC(1:3) GREATER "740" 
                  MOVE "SSN area can't be above 740" TO WS-ERROR-MESSAGE   
               WHEN OTHER
                  CONTINUE
           END-EVALUATE
           .

       5220-VALIDATE-HIRE-DATE.
           EVALUATE TRUE
               WHEN EMPL-HIRE-DATE-YYYYMMDD IN EMPL-IN 
                        LESS THAN "20160212"
                  MOVE "Hire date can't be before the company existed"
                      TO WS-ERROR-MESSAGE       
               WHEN EMPL-HIRE-DATE-YYYYMMDD IN EMPL-IN 
                        GREATER THAN WS-CURRENT-DATE
                  MOVE "Hire date can't be after the current date" 
                      TO WS-ERROR-MESSAGE     
               WHEN OTHER
                  CONTINUE
           END-EVALUATE
           .    

       5400-WRITE-VALIDATED-REC.
           WRITE EMPL-VALIDATED-REC FROM EMPL-IN-REC
           IF NOT EMPL-VALD-OK
               STRING "Unexpected validated file status on write "
                   DELIMITED BY SIZE
                   EMPL-VALIDATED-STATUS
                   DELIMITED BY SIZE
                 INTO WS-ERROR-MESSAGE
               PERFORM 9999-ABORT
           END-IF
           ADD 1 TO WS-VALID-COUNT
           .

       5600-WRITE-ERROR-REC.
           MOVE EMPL-IN-REC TO EMPL-ERRORS-REC 
           MOVE WS-ERROR-MESSAGE TO EMPL-ERROR-MESSAGE
           WRITE EMPL-ERRORS-REC
           IF NOT EMPL-ERRORS-OK
               STRING "Unexpected error file status on write "
                   DELIMITED BY SIZE
                   EMPL-VALIDATED-STATUS
                   DELIMITED BY SIZE
                 INTO WS-ERROR-MESSAGE
               PERFORM 9999-ABORT
           END-IF
           ADD 1 TO WS-ERROR-COUNT
           .

       8000-HOUSEKEEPING.
           CLOSE EMPL-ERRORS
           CLOSE EMPL-VALIDATED.
           CLOSE EMPL-IN
           MOVE WS-VALID-COUNT TO WS-COUNT-FORMATTED
           DISPLAY "Valid records: " WS-COUNT-FORMATTED
           MOVE WS-ERROR-COUNT TO WS-COUNT-FORMATTED
           DISPLAY "Errored records: " WS-COUNT-FORMATTED
           .
       9999-ABORT.
           DISPLAY WS-ERROR-MESSAGE
           GOBACK
           .