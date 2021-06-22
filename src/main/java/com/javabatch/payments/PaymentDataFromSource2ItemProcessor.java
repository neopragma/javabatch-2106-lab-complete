package com.javabatch.payments;

import org.springframework.batch.item.ItemProcessor;

public class PaymentDataFromSource2ItemProcessor 
        implements ItemProcessor<PaymentDataFromSource2, FormattedPaymentData> {

	@Override
	public FormattedPaymentData process(PaymentDataFromSource2 itemFromSource2) throws Exception {				
		return new FormattedPaymentData(
				itemFromSource2.getCustomerId(),
				itemFromSource2.getInvoiceNumber(),
				itemFromSource2.getDueDate(),
				itemFromSource2.getPaidDate(),
				itemFromSource2.getAmountPaid(),
				itemFromSource2.getTaxPaid());
		}
	}