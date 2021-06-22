package com.javabatch.payments;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

// tag::setup[]
@Configuration
@EnableBatchProcessing
public class ProcessPaymentsBatchConfiguration {

	@Autowired
	public JobBuilderFactory processPaymentsJobBuilderFactory;

	@Autowired
	public StepBuilderFactory processPaymentsStepBuilderFactory;
	// end::setup[]

	// tag::readerwriterprocessor[]
	
	// Step 1
	
	@Bean
	@StepScope
	public FlatFileItemReader<PaymentDataFromSource1> paymentDataFromSource1ItemReader(
			@Value("#{jobParameters['PMTSRC1'] ?: 'data/payment-data-from-source-1.txt'}") String paymentDataFromSource1Path) {
		FixedLengthTokenizer lineTokenizer = new FixedLengthTokenizer();
		lineTokenizer.setNames(
				"customerGroupPrefix",
		        "customerId",
		        "invoiceNumber",
		        "amountPaidAsString",
		        "taxPaidAsString",
		        "dueDateAsYYYYMMDD",
		        "paidDateAsYYYYMMDD");
		lineTokenizer.setColumns(
				new Range(20,20),
				new Range(21,37),
				new Range(49,61),
				new Range(65,75),
				new Range(119,125),
				new Range(91,98),
				new Range(99,106));
		
		DefaultLineMapper<PaymentDataFromSource1> paymentDataFromSource1LineMapper = new DefaultLineMapper<>();
		paymentDataFromSource1LineMapper.setLineTokenizer(lineTokenizer);
		paymentDataFromSource1LineMapper.setFieldSetMapper((FieldSetMapper<PaymentDataFromSource1>) new PaymentDataFromSource1FieldSetMapper());
		paymentDataFromSource1LineMapper.afterPropertiesSet();
		
		return new FlatFileItemReaderBuilder<PaymentDataFromSource1>()
			.resource(new FileSystemResource(paymentDataFromSource1Path))	
			.encoding("UTF-8")
			.lineMapper(paymentDataFromSource1LineMapper)
			.strict(true)
			.name("paymentDataFromSource1ItemReader")
			.build();
	}	
	
	@Bean
	public ItemProcessor<PaymentDataFromSource1, FormattedPaymentDataFromSource1> paymentDataFromSource1ItemProcessor() {
		return new PaymentDataFromSource1ItemProcessor();
    }
	
	@Bean
	@StepScope
	public FlatFileItemWriter<FormattedPaymentDataFromSource1> formattedPaymentDataFromSource1ItemWriter(
			@Value("#{jobParameters['PMTFMT1'] ?: 'data/formatted-payment-data-from-source-1.txt'}") String formattedPaymentDataFromSource1Path) {
		return new FlatFileItemWriterBuilder<FormattedPaymentDataFromSource1>()
			.resource(new FileSystemResource(formattedPaymentDataFromSource1Path))
			.lineAggregator(new FormattedPaymentDataFromSource1LineAggregator())
			.encoding("UTF-8")
			.name("formattedPaymentDataFromSource1ItemWriter")
			.shouldDeleteIfExists(true)
			.build();
	}

	
	// Step 2
	
	@Bean
	@StepScope
	public FlatFileItemReader<PaymentDataFromSource2> paymentDataFromSource2ItemReader(
			@Value("#{jobParameters['PMTSRC2'] ?: 'data/payment-data-from-source-2.csv'}") String paymentDataFromSource2Path) {
		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		lineTokenizer.setDelimiter(";");
		lineTokenizer.setNames(
		        "customerId",
		        "invoiceNumber",
		        "amountPaid",
		        "taxPaid",
		        "paidDate",
		        "dueDate");
		
		DefaultLineMapper<PaymentDataFromSource2> paymentDataFromSource2LineMapper = new DefaultLineMapper<>();
		paymentDataFromSource2LineMapper.setLineTokenizer(lineTokenizer);
		paymentDataFromSource2LineMapper.setFieldSetMapper((
				FieldSetMapper<PaymentDataFromSource2>) new PaymentDataFromSource2FieldSetMapper());
		paymentDataFromSource2LineMapper.afterPropertiesSet();
		
		return new FlatFileItemReaderBuilder<PaymentDataFromSource2>()
			.resource(new FileSystemResource(paymentDataFromSource2Path))	
			.linesToSkip(1)
			.encoding("UTF-8")
			.lineMapper(paymentDataFromSource2LineMapper)
			.strict(true)
			.name("paymentDataFromSource2ItemReader")
			.build();
	}	
	
	@Bean
	public ItemProcessor<PaymentDataFromSource2, FormattedPaymentDataFromSource2> paymentDataFromSource2ItemProcessor() {
		return new PaymentDataFromSource2ItemProcessor();
    }
	
	@Bean
	@StepScope
	public FlatFileItemWriter<FormattedPaymentDataFromSource2> formattedPaymentDataFromSource2ItemWriter(
			@Value("#{jobParameters['PMTFMT2'] ?: 'data/formatted-payment-data-from-source-2.txt'}") String formattedPaymentDataFromSource2Path) {
		return new FlatFileItemWriterBuilder<FormattedPaymentDataFromSource2>()
			.resource(new FileSystemResource(formattedPaymentDataFromSource2Path))
			.lineAggregator(new FormattedPaymentDataFromSource2LineAggregator())
			.encoding("UTF-8")
			.name("formattedPaymentDataFromSource2ItemWriter")
			.shouldDeleteIfExists(true)
			.build();
	}
	// end::readerwriterprocessor[]

	// tag::jobstep[]

	@Bean
	public Step formatPaymentDataFromSource1Step(
			FlatFileItemReader<PaymentDataFromSource1> paymentDataFromSource1ItemReader,
			ItemProcessor<PaymentDataFromSource1, FormattedPaymentDataFromSource1> paymentDataFromSource1ItemProcessor,
			FlatFileItemWriter<FormattedPaymentDataFromSource1> formattedPaymentDataFromSource1ItemWriter)
			    throws Exception {
		return processPaymentsStepBuilderFactory.get("formatPaymentDataFromSource1Step")
			.<PaymentDataFromSource1, FormattedPaymentDataFromSource1> chunk(10)
			.reader(paymentDataFromSource1ItemReader(""))
			.processor(paymentDataFromSource1ItemProcessor)
			.writer(formattedPaymentDataFromSource1ItemWriter(""))
			.build();
	}

	@Bean
	public Step formatPaymentDataFromSource2Step(
			FlatFileItemReader<PaymentDataFromSource2> paymentDataFromSource2ItemReader,
			ItemProcessor<PaymentDataFromSource2, FormattedPaymentDataFromSource2> paymentDataFromSource2ItemProcessor,
			FlatFileItemWriter<FormattedPaymentDataFromSource2> formattedPaymentDataFromSource2ItemWriter)
			    throws Exception {
		return processPaymentsStepBuilderFactory.get("formatPaymentDataFromSource2Step")
			.<PaymentDataFromSource2, FormattedPaymentDataFromSource2> chunk(10)
			.reader(paymentDataFromSource2ItemReader(""))
			.processor(paymentDataFromSource2ItemProcessor)
			.writer(formattedPaymentDataFromSource2ItemWriter(""))
			.build();
	}
	
	@Bean
	public Job processPaymentsJob(
			ProcessPaymentsJobCompletionNotificationListener processPaymentsListener) throws Exception {
//			Step formatPaymentDataFromSource1Step,
//			Step formatPaymentDataFromSource2Step) throws Exception {
		return processPaymentsJobBuilderFactory.get("processPaymentsJob")
			.incrementer(new RunIdIncrementer())
			.listener(processPaymentsListener)
			.start(formatPaymentDataFromSource1Step(
					paymentDataFromSource1ItemReader(""), 
					paymentDataFromSource1ItemProcessor(), 
					formattedPaymentDataFromSource1ItemWriter("")))
			.next(formatPaymentDataFromSource2Step(
					paymentDataFromSource2ItemReader(""), 
					paymentDataFromSource2ItemProcessor(), 
					formattedPaymentDataFromSource2ItemWriter("")))
			.build();
	}
	// end::jobstep[]
}
