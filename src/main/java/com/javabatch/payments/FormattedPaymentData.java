package com.javabatch.payments;

import java.util.Calendar;

import org.javamoney.moneta.Money;

public class FormattedPaymentData {
	
	private String customerId;
	private String invoiceNumber;
	private Calendar dateDue;
	private Calendar datePaid;
	private Money amountPaid;
	private Money taxPaid;
	
	public FormattedPaymentData(
			String customerId, 
			String invoiceNumber,
			Calendar dateDue,
			Calendar datePaid,
			Money amountPaid,
			Money taxPaid) {
		this.customerId = customerId;
		this.invoiceNumber = invoiceNumber;
		this.dateDue = dateDue;
		this.datePaid = datePaid;
		this.amountPaid = amountPaid;
		this.taxPaid = taxPaid;
	}

	public String getCustomerId() {
		return customerId;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public Calendar getDateDue() {
		return dateDue;
	}

	public Calendar getDatePaid() {
		return datePaid;
	}

	public Money getAmountPaid() {
		return amountPaid;
	}

	public Money getTaxPaid() {
		return taxPaid;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public void setDateDue(Calendar dateDue) {
		this.dateDue = dateDue;
	}

	public void setDatePaid(Calendar datePaid) {
		this.datePaid = datePaid;
	}

	public void setAmountPaid(Money amountPaid) {
		this.amountPaid = amountPaid;
	}

	public void setTaxPaid(Money taxPaid) {
		this.taxPaid = taxPaid;
	}

}
