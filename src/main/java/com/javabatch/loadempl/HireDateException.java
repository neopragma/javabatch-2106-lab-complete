package com.javabatch.loadempl;

public class HireDateException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private String message;
	public HireDateException(String message) {
		this.message = message;
	}
	public String getMessage() {
		return message;
	}	
}
