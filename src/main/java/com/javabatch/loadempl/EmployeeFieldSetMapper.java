package com.javabatch.loadempl;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class EmployeeFieldSetMapper implements FieldSetMapper<Employee> {

	@Override
	public Employee mapFieldSet(FieldSet fieldSet) throws BindException {
		return new Employee(
				fieldSet.readString("name"),
				fieldSet.readString("hireDateAsYYYYMMDD"),
				fieldSet.readString("ssn"));
	}
}
