package com.javabatch.examples;

public class SsnException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	private String message;
	
	public SsnException(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}

}
