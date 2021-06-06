package com.javabatch.loadempl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class HireDate {
	
	private String currentDateAsYYYYMMDD;
	private String hireDateAsYYYYMMDD;
	
	public HireDate(String hireDateAsYYYYMMDD) {
		this(hireDateAsYYYYMMDD, Instant.now());
	}
	
	public HireDate(String hireDateAsYYYYMMDD, Instant currentDate) {
		this.hireDateAsYYYYMMDD = hireDateAsYYYYMMDD;
		LocalDateTime ldt = LocalDateTime.ofInstant(currentDate, ZoneId.systemDefault());
		currentDateAsYYYYMMDD =  String.format("%04d%02d%02d", ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth());
		
		if (hireDateAsYYYYMMDD.compareTo("20160212") < 0) {
			throw new HireDateException("Hire date can't be before the company existed");
		}
		
		if (hireDateAsYYYYMMDD.compareTo(currentDateAsYYYYMMDD) > 0) {
			throw new HireDateException("Hire date can't be after the current date");
		}
	}
	
	public String getHireDateAsYYYYMMDD() {
		return hireDateAsYYYYMMDD;
	}
}
