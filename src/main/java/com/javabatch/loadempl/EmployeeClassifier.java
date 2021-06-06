package com.javabatch.loadempl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.classify.Classifier;

public class EmployeeClassifier implements Classifier<Employee, ItemWriter> {

	private static final Logger log = LoggerFactory.getLogger(LoadEmplJobCompletionNotificationListener.class);
	
	@Autowired
	ItemWriter validEmployeeItemWriter;
	
	@Override
	public ItemWriter classify(Employee classifiable) {
		
		log.error("validEmployeeItemWriter: " + validEmployeeItemWriter.getClass().getName());
		
		return validEmployeeItemWriter;
	}

}
