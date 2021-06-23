package com.javabatch.examples;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.ItemReader;
import javax.inject.Inject;

public class SampleFixedLengthItemReader implements ItemReader {
	
	@Inject
    @BatchProperty
    private String inputFilePath;
	
	private BufferedReader inputFileReader;	

	@Override
	public void open(Serializable checkpoint) throws Exception {
	    inputFileReader = new BufferedReader(new FileReader(inputFilePath));
	}

	@Override
	public void close() throws Exception {
        inputFileReader.close();
	}

	@Override
	public Object readItem() throws Exception {
        String inputLine;
        try {
        	while ((inputLine = inputFileReader.readLine()) != null) {
                return new NewHire(
                		inputLine.substring(0,80),             // employee name
                		inputLine.substring(80,88),            // hire date
                		new SSN(inputLine.substring(88,97)));  // social security number
        	}
        	return null;                                       // end of file
        } catch (IOException ioe) {
        	throw new RuntimeException("IOException reading new hire file", ioe);
        } catch (Exception ex) {
        	throw new RuntimeException("Unexpected exception reading new hire file", ex);
        }
	}

	@Override
	public Serializable checkpointInfo() throws Exception {
		return null;
	}

}
