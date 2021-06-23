package com.javabatch.examples;

public class SSN {
	
	private String areaNumber;
	private String groupNumber;
	private String serialNumber;
	
	public SSN(String ssnAsString) {
		if (ssnAsString == null) {
			throw new SsnException("SSN value can't be null.");
		}
		if (ssnAsString.length() != 9) {
			throw new SsnException(
					"SSN value must be 9 characters long. Value \""
			      + ssnAsString + "\" is " + ssnAsString.length() + " characters long.");
		}
		areaNumber = ssnAsString.substring(0,3);
		groupNumber = ssnAsString.substring(3,5);
		serialNumber = ssnAsString.substring(5);
		if (!areaNumber.startsWith("T")) {
			if (areaNumber.equals("666")) {
				throw new SsnException("SSN area can't be 666");
			}
			if (areaNumber.compareTo("740") > 0) {
				throw new SsnException("SSN area can't be above 740");
			}
		}
	}

	public String getAreaNumber() {
		return areaNumber;
	}

	public String getGroupNumber() {
		return groupNumber;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

}
