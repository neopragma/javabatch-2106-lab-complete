package com.javabatch.payments.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import javax.money.Monetary;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;

public class MoneyConversionTest {
	
	@Test
	public void itConvertsStringRepresenting84dollarsAnd26centsToMoney() {
		Money expectedResult = Money.of(new BigDecimal("84.26"), Monetary.getCurrency("USD"));
		assertEquals(expectedResult, Convert.stringToMoney("00008426"));
	}

}
