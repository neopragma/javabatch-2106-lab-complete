package com.javabatch.loadempl;

import org.springframework.batch.item.file.transform.LineAggregator;

public class EmployeeWithErrorsLineAggregator implements LineAggregator<Employee> {

	@Override
	public String aggregate(Employee employee) {
		
        System.getProperty("line.separator");
		
		StringBuilder sb = new StringBuilder();
		sb.append(fixedLength(employee.getName(), 80));
		sb.append(fixedLength(employee.getHireDateAsYYYYMMDD(), 8));
		sb.append(fixedLength(employee.getSsn(), 9));
		sb.append(fixedLength(employee.getErrorMessage(), 50));
		return sb.toString();
	}
	
	private String fixedLength(String value, int length) {
		return String.format("%1$-" + length + "s", value).substring(0, length);	
	}

}
