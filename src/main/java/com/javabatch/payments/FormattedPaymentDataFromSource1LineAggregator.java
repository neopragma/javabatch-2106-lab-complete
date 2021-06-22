package com.javabatch.payments;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.javamoney.moneta.Money;
import org.springframework.batch.item.file.transform.LineAggregator;

public class FormattedPaymentDataFromSource1LineAggregator implements LineAggregator<FormattedPaymentData> {
	
	@Override
	public String aggregate(FormattedPaymentData paymentDataIn) {
		StringBuilder sb = new StringBuilder();
		sb.append(fixedLength(paymentDataIn.getCustomerId(), 17));
		sb.append(fixedLength(paymentDataIn.getInvoiceNumber(), 13));
		sb.append(fixedLength(calendarToString(paymentDataIn.getDateDue()), 8));
		sb.append(fixedLength(calendarToString(paymentDataIn.getDatePaid()), 8));
		sb.append(fixedLength(moneyToString(paymentDataIn.getAmountPaid()), 18));
		sb.append(fixedLength(moneyToString(paymentDataIn.getTaxPaid()), 14));
		return sb.toString();
	}
	
	String calendarToString(Calendar calendar) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		return format.format(calendar.getTime());
	}
	
	String moneyToString(Money amount) {
		long justTheNumber = amount.getNumberStripped().multiply(new BigDecimal(100)).longValue();
		return String.valueOf(justTheNumber);
	}
	
	private String fixedLength(String value, int length) {
		return String.format("%1$" + length + "s", value).substring(0, length).replace(' ', '0');	
	}
}
