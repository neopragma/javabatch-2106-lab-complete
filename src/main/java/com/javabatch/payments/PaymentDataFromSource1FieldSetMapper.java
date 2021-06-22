package com.javabatch.payments;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class PaymentDataFromSource1FieldSetMapper implements FieldSetMapper<PaymentDataFromSource1> {

	@Override
	public PaymentDataFromSource1 mapFieldSet(FieldSet fieldSet) throws BindException {
		try {
			return new PaymentDataFromSource1(
					fieldSet.readString("customerGroupPrefix"),
					fieldSet.readString("customerId"),
					fieldSet.readString("invoiceNumber"),
					fieldSet.readString("amountPaidAsString"),
					fieldSet.readString("taxPaidAsString"),
					fieldSet.readString("dueDateAsYYYYMMDD"),
					fieldSet.readString("paidDateAsYYYYMMDD"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
