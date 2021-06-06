package com.javabatch.loadempl;

public class Employee {
	
	private String name;
	private String hireDateAsYYYYMMDD;
	private String ssn;
	private String errorMessage;
	private boolean errorFound;
	
	public Employee(String name, String hireDateAsYYYYMMDD, String ssn) {
		this.name = name;
		this.hireDateAsYYYYMMDD = hireDateAsYYYYMMDD;
		this.ssn = ssn;
		this.errorMessage = "";
		this.errorFound = false;
	}

	public String getName() {
		return name;
	}

	public String getHireDateAsYYYYMMDD() {
		return hireDateAsYYYYMMDD;
	}

	public String getSsn() {
		return ssn;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public boolean isErrorFound() {
		return errorFound;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setHireDateAsYYYYMMDD(String hireDateAsYYYYMMDD) {
		this.hireDateAsYYYYMMDD = hireDateAsYYYYMMDD;
	}

	public void setSsn(String ssn) {
		this.ssn = ssn;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public void setErrorFound(boolean errorFound) {
		this.errorFound = errorFound;
	}
	
}
