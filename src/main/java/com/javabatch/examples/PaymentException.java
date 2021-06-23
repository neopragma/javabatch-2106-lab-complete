package com.javabatch.examples;

public class PaymentException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private String message;
	private Throwable cause;
	
	public PaymentException(String message, Throwable cause) {
		this.message = message;
		this.cause = cause;
	}
	
	public String getMessage() {
		return message;
	}
	
	public Throwable getCause() {
		return cause;
	}

}
