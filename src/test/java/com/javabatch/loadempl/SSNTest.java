package com.javabatch.loadempl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

public class SSNTest {
	
	@Test
	public void itRejectsNullSSN() {
		Throwable ex = assertThrows(SsnException.class, () -> {
			new SSN(null);
		});
		assertEquals("SSN value can't be null.", ex.getMessage());
	}
	
	@Test
	public void itRejectsSSNValueTooShort() {
		Throwable ex = assertThrows(SsnException.class, () -> {
			new SSN("12345678");
		});
		assertEquals("SSN value must be 9 characters long. Value \"12345678\" is 8 characters long.", ex.getMessage());
	}
	
	@Test
	public void itRejectsSSNValueTooLong() {
		Throwable ex = assertThrows(SsnException.class, () -> {
			new SSN("1234567876");
		});
		assertEquals("SSN value must be 9 characters long. Value \"1234567876\" is 10 characters long.", ex.getMessage());
	}
	
	@Test
	public void itRejectsSSNArea666() {
		Throwable ex = assertThrows(SsnException.class, () -> {
			new SSN("666123456");
		});
		assertEquals("SSN area can't be 666", ex.getMessage());
	}
	
	@Test
	public void itRejectsSSNArea741() {
		Throwable ex = assertThrows(SsnException.class, () -> {
			new SSN("741123456");
		});
		assertEquals("SSN area can't be above 740", ex.getMessage());
	}
	
	@Test 
	public void itStoresTheValueWhenNoErrorsAreFound() {
		SSN ssn = new SSN("529348162");
		assertEquals("529", ssn.getAreaNumber());
		assertEquals("34", ssn.getGroupNumber());
		assertEquals("8162", ssn.getSerialNumber());
		
	}

	
}
