package com.javabatch.payments;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class PaymentDataFromSource2FieldSetMapper implements FieldSetMapper<PaymentDataFromSource2> {

	@Override
	public PaymentDataFromSource2 mapFieldSet(FieldSet fieldSet) throws BindException {
		try {
			return new PaymentDataFromSource2(
					fieldSet.readString("customerId"),
					fieldSet.readString("invoiceNumber"),
					fieldSet.readString("amountPaid"),
					fieldSet.readString("taxPaid"),
					fieldSet.readString("paidDate"),
					fieldSet.readString("dueDate"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
