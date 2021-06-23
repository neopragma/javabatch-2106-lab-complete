package com.javabatch.examples;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Represents a single input item from the "new hire" input source.
 * 
 * @author neopragma
 */
public class NewHire {
	
	private String employeeName;
	private Instant hireDate;
	private SSN ssn;
	
	public NewHire(String employeeName, String hireDateAsYYYYMMDD, SSN ssn) throws Exception {
		this.employeeName = employeeName;
		this.hireDate = stringToInstant(hireDateAsYYYYMMDD);
		this.ssn = ssn;
	}
	
	private Instant stringToInstant(String dateAsYYYYMMDD) {
		return LocalDateTime.parse(dateAsYYYYMMDD,
				DateTimeFormatter.ofPattern("yyyyMMdd", Locale.US))
				.atZone(ZoneId.of("America/Chicago"))
				.toInstant();
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public Instant getHireDate() {
		return hireDate;
	}

	public SSN getSsn() {
		return ssn;
	}

}
