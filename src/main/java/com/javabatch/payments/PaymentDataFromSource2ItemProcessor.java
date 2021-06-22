package com.javabatch.payments;

import org.springframework.batch.item.ItemProcessor;

public class PaymentDataFromSource2ItemProcessor 
<<<<<<< HEAD
        implements ItemProcessor<PaymentDataFromSource2, FormattedPaymentData> {

	@Override
	public FormattedPaymentData process(PaymentDataFromSource2 itemFromSource2) throws Exception {				
		return new FormattedPaymentData(
=======
        implements ItemProcessor<PaymentDataFromSource2, FormattedPaymentDataFromSource2> {

	@Override
	public FormattedPaymentDataFromSource2 process(PaymentDataFromSource2 itemFromSource2) throws Exception {				
		return new FormattedPaymentDataFromSource2(
>>>>>>> 3c092ca829486fc8c423649a2c5279a490a5d578
				itemFromSource2.getCustomerId(),
				itemFromSource2.getInvoiceNumber(),
				itemFromSource2.getDueDate(),
				itemFromSource2.getPaidDate(),
				itemFromSource2.getAmountPaid(),
				itemFromSource2.getTaxPaid());
		}
	}