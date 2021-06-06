package com.javabatch.loadempl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

public class HireDateTest {
	
	@Test
	public void itRejectsHireDatePriorToCompanyFounding() {
		Throwable ex = assertThrows(HireDateException.class, () -> {
			new HireDate("20160211");
		});
		assertEquals("Hire date can't be before the company existed", ex.getMessage());
	}

	@Test
	public void itRejectsHireDateAfterTheCurrentDate() {
		Throwable ex = assertThrows(HireDateException.class, () -> {
			new HireDate("20251204", Instant.parse("2025-12-03T10:15:30.00Z"));
		});	
		assertEquals("Hire date can't be after the current date", ex.getMessage());
	}
	
	@Test void itStoresTheHireDateWhenNoErrorsAreFound() {
		HireDate hireDate = new HireDate("20251202", Instant.parse("2025-12-03T10:15:30.00Z"));
		assertEquals("20251202", hireDate.getHireDateAsYYYYMMDD());
	}
	
}
