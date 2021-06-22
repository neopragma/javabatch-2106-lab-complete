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
public class PaymentDataFromSource2 {
	private String customerId;
	private String invoiceNumber;
	private Money amountPaid;
	private Money taxPaid;
	private Calendar dueDate;
	private Calendar paidDate;
	private final CurrencyUnit currencyUnit = Monetary.getCurrency("USD");
	private final String decimalPoint = ".";
	private final String inputDateFormat = "yyyyMMdd";
	
	public PaymentDataFromSource2(
			String customerId,
			String invoiceNumber,
			String amountPaidAsString,
			String taxPaidAsString,
			String dueDateAsYYYYMMDDwithSlashes,
			String paidDateAsYYYYMMDDwishSlashes) throws Exception {
		
		this.customerId = customerId;		
		this.invoiceNumber = invoiceNumber;
		this.amountPaid = moneyValueFromString(amountPaidAsString);
		this.taxPaid = moneyValueFromString(taxPaidAsString);		
		this.paidDate = calendarFromString(paidDateAsYYYYMMDDwishSlashes);
		this.dueDate = calendarFromString(dueDateAsYYYYMMDDwithSlashes);
	}
	
	private Money moneyValueFromString(String amountAsString) {
		return Money.of(new BigDecimal(amountAsString), currencyUnit);		
	}
	
	private Calendar calendarFromString(String dateStringAsYYYYMMDDwithSlashes) {
		Calendar date = Calendar.getInstance();
		date.set(Calendar.YEAR, Integer.parseInt(dateStringAsYYYYMMDDwithSlashes.substring(0,4)));
		date.set(Calendar.MONTH, Integer.parseInt(dateStringAsYYYYMMDDwithSlashes.substring(5,7)));
		date.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateStringAsYYYYMMDDwithSlashes.substring(9,10)));
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
