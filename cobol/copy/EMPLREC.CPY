           05  EMPL-NAME               PIC X(80). 
           05  EMPL-HIRE-DATE-YYYYMMDD PIC X(08).
           05  EMPL-SSN.
               10  FILLER              PIC X.
                   88  TEST-SSN        VALUE 'T'.
               10  FILLER              PIC X(08).    