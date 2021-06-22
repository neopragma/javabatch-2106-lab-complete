package com.javabatch.payments.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;

import org.junit.jupiter.api.Test;

public class DateConversionTest {
	
	@Test
	public void itConvertsValidDateStringToCalendarObject() {
		Calendar expectedResult = Calendar.getInstance();
		expectedResult.set(2023, 5, 14);
		Calendar actualResult = Convert.yyyymmddToCalendar("20230614");
		assertEquals(expectedResult.get(Calendar.YEAR), actualResult.get(Calendar.YEAR));
		assertEquals(expectedResult.get(Calendar.MONTH), actualResult.get(Calendar.MONTH));
		assertEquals(expectedResult.get(Calendar.DAY_OF_MONTH), actualResult.get(Calendar.DAY_OF_MONTH));
	}

}
