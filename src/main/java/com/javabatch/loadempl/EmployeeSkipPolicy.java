package com.javabatch.loadempl;

import java.io.FileNotFoundException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.transform.IncorrectLineLengthException;

public class EmployeeSkipPolicy implements SkipPolicy {
	     
    private static final Logger log = LoggerFactory.getLogger("EmployeeSkipPolicy");
    
    private final static String errorMessage = 
    		"wrong length record: line %1$s, value <%2$s>." + System.getProperty("line.separator");
    private final static String lengthMessage = 
    		"expected length: %1$s, actual length: %2$s";
 
    @Override
    public boolean shouldSkip(Throwable exception, int skipCount) throws SkipLimitExceededException {
        if (exception instanceof FileNotFoundException) {
            return false;
        } else if (exception instanceof FlatFileParseException && skipCount <= 5) {
            FlatFileParseException ffpe = (FlatFileParseException) exception;
            log.warn(String.format(errorMessage, ffpe.getLineNumber(), ffpe.getInput()));
            Throwable cause = ffpe.getCause();
            if (cause instanceof IncorrectLineLengthException) {
            	IncorrectLineLengthException ille = (IncorrectLineLengthException) cause;
            	log.warn(String.format(lengthMessage, ille.getExpectedLength(), ille.getActualLength()));
            };
            return true;
        } else {
            return false;
        }
    }
 
}
