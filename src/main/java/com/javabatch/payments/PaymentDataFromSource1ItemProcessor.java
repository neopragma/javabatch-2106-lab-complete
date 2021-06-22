package com.javabatch.payments;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.batch.item.ItemProcessor;

public class PaymentDataFromSource1ItemProcessor 
<<<<<<< HEAD
        implements ItemProcessor<PaymentDataFromSource1, FormattedPaymentData> {

	@Override
	public FormattedPaymentData process(PaymentDataFromSource1 itemFromSource1) throws Exception {
		LocalDate dueDate = LocalDate.ofInstant(itemFromSource1.getDueDate().toInstant(), ZoneId.systemDefault());
		LocalDate threshholdDate = LocalDate.ofInstant(Instant.now(), ZoneId.systemDefault()).minusDays(90L);
		if (dueDate.compareTo(threshholdDate) > -1) {
		return new FormattedPaymentData(
=======
        implements ItemProcessor<PaymentDataFromSource1, FormattedPaymentDataFromSource1> {

	@Override
	public FormattedPaymentDataFromSource1 process(PaymentDataFromSource1 itemFromSource1) throws Exception {
		LocalDate dueDate = LocalDate.ofInstant(itemFromSource1.getDueDate().toInstant(), ZoneId.systemDefault());
		LocalDate threshholdDate = LocalDate.ofInstant(Instant.now(), ZoneId.systemDefault()).minusDays(90L);
		if (dueDate.compareTo(threshholdDate) > -1) {
		return new FormattedPaymentDataFromSource1(
>>>>>>> 3c092ca829486fc8c423649a2c5279a490a5d578
				itemFromSource1.getCustomerId(),
				itemFromSource1.getInvoiceNumber(),
				itemFromSource1.getDueDate(),
				itemFromSource1.getPaidDate(),
				itemFromSource1.getAmountPaid(),
				itemFromSource1.getTaxPaid());
		} else {
			return null;
		}
	}

}
