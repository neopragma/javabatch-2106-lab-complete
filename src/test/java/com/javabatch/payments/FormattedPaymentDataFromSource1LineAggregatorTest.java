package com.javabatch.payments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FormattedPaymentDataFromSource1LineAggregatorTest {
	
	private FormattedPaymentDataFromSource1LineAggregator subject;
	private CurrencyUnit currencyUnit = Monetary.getCurrency("USD");

	
	@BeforeEach
	public void commonSetup() {
		subject = new FormattedPaymentDataFromSource1LineAggregator();
	}
	
	@Test 
	public void itConvertsCalendarValueToString() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2345);
		cal.set(Calendar.MONTH, 5);
		cal.set(Calendar.DAY_OF_MONTH, 15);
		assertEquals("23450615", subject.calendarToString(cal));
	}
	
	@Test 
	public void itConvertsMoneyValueToString() {
        Money amount = Money.of(12345.67, currencyUnit);
        assertEquals("1234567", subject.moneyToString(amount));
	}

}
