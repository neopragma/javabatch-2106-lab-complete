package com.javabatch.payments;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

import org.javamoney.moneta.Money;

/**
 * Encapsulates data from the file provided by external source 1 for payments received.
 * @author neopragma
 */
public class PaymentDataFromSource1 {
	private String customerId;
	private String invoiceNumber;
	private Money amountPaid;
	private Money taxPaid;
	private Calendar dueDate;
	private Calendar paidDate;
	private final CurrencyUnit currencyUnit = Monetary.getCurrency("USD");
	private final String decimalPoint = ".";
	private final String inputDateFormat = "yyyyMMdd";
	
	public PaymentDataFromSource1(
			String customerGroupPrefix,
			String customerId,
			String invoiceNumber,
			String amountPaidAsString,
			String taxPaidAsString,
			String dueDateAsYYYYMMDD,
			String paidDateAsYYYYMMDD) throws Exception {
		
		this.customerId = fixCustomerIdIfNecessary(customerGroupPrefix, customerId);		
		this.invoiceNumber = invoiceNumber;
		this.amountPaid = moneyValueFromString(amountPaidAsString);
		this.taxPaid = moneyValueFromString(taxPaidAsString);		
		this.dueDate = calendarFromString(dueDateAsYYYYMMDD);
		this.paidDate = calendarFromString(paidDateAsYYYYMMDD);
	}
	
	private String fixCustomerIdIfNecessary(String customerGroupPrefix, String customerId) {
		String internalCustomerPrefix = "";
		if (customerGroupPrefix.equals("N")) {
			internalCustomerPrefix = "42";
		} else if (customerGroupPrefix.equals("S")) {
			internalCustomerPrefix = "64";
		}
		if (internalCustomerPrefix.equals("")) {
			return customerId;
		} else {
			return internalCustomerPrefix + customerId.substring(2);
		}	
	}
	
	private Money moneyValueFromString(String originalValue) {
		int impliedDecimalPoint = originalValue.length()-2;
		BigDecimal amount = new BigDecimal(
				originalValue.substring(0,impliedDecimalPoint)
		      + decimalPoint 
			  +	originalValue.substring(impliedDecimalPoint));
		return Money.of(amount, currencyUnit);		
	}
	
	private Calendar calendarFromString(String originalValue) {
		Calendar date = Calendar.getInstance();
		date.set(Calendar.YEAR, Integer.parseInt(originalValue.substring(0,4)));
		date.set(Calendar.MONTH, Integer.parseInt(originalValue.substring(4,6)));
		date.set(Calendar.DAY_OF_MONTH, Integer.parseInt(originalValue.substring(6,8)));
		return date;
	}

	public String getCustomerId() {
		return customerId;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public Money getAmountPaid() {
		return amountPaid;
	}

	public Money getTaxPaid() {
		return taxPaid;
	}

	public Calendar getDueDate() {
		return dueDate;
	}

	public Calendar getPaidDate() {
		return paidDate;
	}

	public CurrencyUnit getCurrencyUnit() {
		return currencyUnit;
	}

	public String getDecimalPoint() {
		return decimalPoint;
	}

	public String getInputDateFormat() {
		return inputDateFormat;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public void setAmountPaid(Money amountPaid) {
		this.amountPaid = amountPaid;
	}

	public void setTaxPaid(Money taxPaid) {
		this.taxPaid = taxPaid;
	}

	public void setDueDate(Calendar dueDate) {
		this.dueDate = dueDate;
	}

	public void setPaidDate(Calendar paidDate) {
		this.paidDate = paidDate;
	}
	
}
