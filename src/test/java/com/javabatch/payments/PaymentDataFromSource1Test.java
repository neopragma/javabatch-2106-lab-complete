package com.javabatch.payments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PaymentDataFromSource1Test {
	
	private PaymentDataFromSource1 subject;
	private final CurrencyUnit currencyUnit = Monetary.getCurrency("USD");	
	
	@BeforeEach
	public void commonSetup() throws Exception {
    	subject = new PaymentDataFromSource1(
    			"X",
    			"12345678901234567",
    			"1234567890123",
    			"00000123498",
    			"0004466",
    			"20200615",
    			"20200516"
    			);
	}

    @Test
    public void itHandlesCustomerIdThatRequiresNoCorrection() throws Exception {
    	assertEquals("12345678901234567", subject.getCustomerId());    	
    }

    @Test
    public void itPlugsIn42WhenCustomerGroupIsN() throws Exception {
    	PaymentDataFromSource1 subject = new PaymentDataFromSource1(
    			"N",
    			"12345678901234567",
    			"1234567890123",
    			"00000123498",
    			"0004466",
    			"20200615",
    			"20200516"
    			);
    	assertEquals("42345678901234567", subject.getCustomerId());    	
    }

    @Test
    public void itPlugsIn64WhenCustomerGroupIsS() throws Exception {
    	PaymentDataFromSource1 subject = new PaymentDataFromSource1(
    			"S",
    			"12345678901234567",
    			"1234567890123",
    			"00000123498",
    			"0004466",
    			"20200615",
    			"20200516"
    			);
    	assertEquals("64345678901234567", subject.getCustomerId());    	
    }

    @Test
    public void itConvertsAmountPaidFromStringToMoney() throws Exception {
    	assertEquals(Money.of(1234.98, currencyUnit), subject.getAmountPaid());    	
    }

    @Test
    public void itConvertsTaxPaidFromStringToMoney() throws Exception {
    	assertEquals(Money.of(44.66, currencyUnit), subject.getTaxPaid());    	
    }
	
	@Test
	public void itConvertsDueDateFromString() throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.set(2020, 5, 15);
		assertEquals(2020, subject.getDueDate().get(Calendar.YEAR));
		assertEquals(6, subject.getDueDate().get(Calendar.MONTH));
		assertEquals(15, subject.getDueDate().get(Calendar.DAY_OF_MONTH));
	}
	
	@Test
	public void itConvertsPaidDateFromString() throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.set(2020, 4, 16);
		assertEquals(2020, subject.getPaidDate().get(Calendar.YEAR));
		assertEquals(5, subject.getPaidDate().get(Calendar.MONTH));
		assertEquals(16, subject.getPaidDate().get(Calendar.DAY_OF_MONTH));
	}
}
