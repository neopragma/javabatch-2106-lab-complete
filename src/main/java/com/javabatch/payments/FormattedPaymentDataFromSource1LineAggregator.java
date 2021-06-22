package com.javabatch.payments;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.javamoney.moneta.Money;
import org.springframework.batch.item.file.transform.LineAggregator;

<<<<<<< HEAD
public class FormattedPaymentDataFromSource1LineAggregator implements LineAggregator<FormattedPaymentData> {
	
	@Override
	public String aggregate(FormattedPaymentData paymentDataIn) {
=======
public class FormattedPaymentDataFromSource1LineAggregator implements LineAggregator<FormattedPaymentDataFromSource1> {
	
	@Override
	public String aggregate(FormattedPaymentDataFromSource1 paymentDataIn) {
>>>>>>> 3c092ca829486fc8c423649a2c5279a490a5d578
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
<<<<<<< HEAD
		return String.format("%1$" + length + "s", value).substring(0, length).replace(' ', '0');	
=======
		return String.format("%1$-" + length + "s", value).substring(0, length);	
>>>>>>> 3c092ca829486fc8c423649a2c5279a490a5d578
	}
}
