package com.javabatch.payments;

import javax.batch.api.Batchlet;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet;
import org.springframework.batch.core.step.tasklet.Tasklet;
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
	
	//------------------------------------------------------------
	// Step 1 - read data from external partner 1, filter records, edit in-stream, reformat to our internal format
	//------------------------------------------------------------
	
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
	public ItemProcessor<PaymentDataFromSource1, FormattedPaymentData> paymentDataFromSource1ItemProcessor() {
		return new PaymentDataFromSource1ItemProcessor();
    }
	
	@Bean
	@StepScope
	public FlatFileItemWriter<FormattedPaymentData> formattedPaymentDataFromSource1ItemWriter(
			@Value("#{jobParameters['PMTFMT1'] ?: 'data/formatted-payment-data-from-source-1.txt'}") String formattedPaymentDataFromSource1Path) {
		return new FlatFileItemWriterBuilder<FormattedPaymentData>()
			.resource(new FileSystemResource(formattedPaymentDataFromSource1Path))
			.lineAggregator(new FormattedPaymentDataFromSource1LineAggregator())
			.encoding("UTF-8")
			.name("formattedPaymentDataFromSource1ItemWriter")
			.shouldDeleteIfExists(true)
			.build();
	}

	@Bean
	public Step formatPaymentDataFromSource1Step(
			FlatFileItemReader<PaymentDataFromSource1> paymentDataFromSource1ItemReader,
			ItemProcessor<PaymentDataFromSource1, FormattedPaymentData> paymentDataFromSource1ItemProcessor,
			FlatFileItemWriter<FormattedPaymentData> formattedPaymentDataFromSource1ItemWriter)
			    throws Exception {
		return processPaymentsStepBuilderFactory.get("formatPaymentDataFromSource1Step")
			.<PaymentDataFromSource1, FormattedPaymentData> chunk(10)
			.reader(paymentDataFromSource1ItemReader(""))
			.processor(paymentDataFromSource1ItemProcessor)
			.writer(formattedPaymentDataFromSource1ItemWriter(""))
			.build();
	}

	//------------------------------------------------------------
	// Step 2 - sort the formatted input records from external partner 1
	//------------------------------------------------------------
	
	@Bean
	@StepScope
	public SystemCommandTasklet sortPaymentDataFromSource1(
			@Value("#{jobParameters['PMTFMT1'] ?: 'data/formatted-payment-data-from-source-1.txt'}") String inputToSort,
			@Value("#{jobParameters['PMTSRT1'] ?: 'data/sorted-payment-data-from-source-1.txt'}") String outputFromSort) {
	  SystemCommandTasklet tasklet = new SystemCommandTasklet();    
	  tasklet.setCommand("./sortfile " + inputToSort + " " + outputFromSort);
	  tasklet.setTimeout(5000);
	  tasklet.setWorkingDirectory(System.getProperty("user.dir"));
	  return tasklet;
    }

	@Bean
	public Step sortPaymentDataFromSource1Step(
			SystemCommandTasklet sortPaymentDataFromSource1) {
		return processPaymentsStepBuilderFactory
			.get("sortPaymentDataFromSource1Step")
			.tasklet(sortPaymentDataFromSource1("",""))
			.build();
	}
	
	//------------------------------------------------------------
	// Step 3 - read input from external partner 2, reformat to our internal format
	//------------------------------------------------------------
	
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
	public ItemProcessor<PaymentDataFromSource2, FormattedPaymentData> paymentDataFromSource2ItemProcessor() {
		return new PaymentDataFromSource2ItemProcessor();
    }
	
	@Bean
	@StepScope
	public FlatFileItemWriter<FormattedPaymentData> formattedPaymentDataFromSource2ItemWriter(
			@Value("#{jobParameters['PMTFMT2'] ?: 'data/formatted-payment-data-from-source-2.txt'}") String formattedPaymentDataFromSource2Path) {
		return new FlatFileItemWriterBuilder<FormattedPaymentData>()
			.resource(new FileSystemResource(formattedPaymentDataFromSource2Path))
			.lineAggregator(new FormattedPaymentDataFromSource2LineAggregator())
			.encoding("UTF-8")
			.name("formattedPaymentDataFromSource2ItemWriter")
			.shouldDeleteIfExists(true)
			.build();
	}
	// end::readerwriterprocessor[]

	@Bean
	public Step formatPaymentDataFromSource2Step(
			FlatFileItemReader<PaymentDataFromSource2> paymentDataFromSource2ItemReader,
			ItemProcessor<PaymentDataFromSource2, FormattedPaymentData> paymentDataFromSource2ItemProcessor,
			FlatFileItemWriter<FormattedPaymentData> formattedPaymentDataFromSource2ItemWriter)
			    throws Exception {
		return processPaymentsStepBuilderFactory.get("formatPaymentDataFromSource2Step")
			.<PaymentDataFromSource2, FormattedPaymentData> chunk(10)
			.reader(paymentDataFromSource2ItemReader(""))
			.processor(paymentDataFromSource2ItemProcessor)
			.writer(formattedPaymentDataFromSource2ItemWriter(""))
			.build();
	}

	//------------------------------------------------------------
	// Step 4 - sort the formatted input records from external partner 2
	//------------------------------------------------------------
	
	@Bean
	@StepScope
	public SystemCommandTasklet sortPaymentDataFromSource2(
			@Value("#{jobParameters['PMTFMT2'] ?: 'data/formatted-payment-data-from-source-2.txt'}") String inputToSort,
			@Value("#{jobParameters['PMTSRT2'] ?: 'data/sorted-payment-data-from-source-2.txt'}") String outputFromSort) {
	  SystemCommandTasklet tasklet = new SystemCommandTasklet();    
	  tasklet.setCommand("./sortfile " + inputToSort + " " + outputFromSort);
	  tasklet.setTimeout(5000);
	  tasklet.setWorkingDirectory(System.getProperty("user.dir"));
	  return tasklet;
    }

	@Bean
	public Step sortPaymentDataFromSource2Step(
			SystemCommandTasklet sortPaymentDataFromSource2) {
		return processPaymentsStepBuilderFactory
			.get("sortPaymentDataFromSource2Step")
			.tasklet(sortPaymentDataFromSource2("",""))
			.build();
	}

	//------------------------------------------------------------
	// Step 5 - merge the sorted input files
	//------------------------------------------------------------
	
	@Bean
	@StepScope
	public SystemCommandTasklet mergePaymentData(
			@Value("#{jobParameters['PMTSRT'] ?: 'data/sorted-payment-data*'}") String filesToMerge,
			@Value("#{jobParameters['PMTMRG'] ?: 'data/merged-payment-data.txt'}") String mergedPaymentData) {		
	  SystemCommandTasklet tasklet = new SystemCommandTasklet();    
	  tasklet.setCommand("./mergefiles " + filesToMerge + " " + mergedPaymentData);
	  tasklet.setTimeout(5000);
	  tasklet.setWorkingDirectory(System.getProperty("user.dir"));
	  return tasklet;
    }

	@Bean
	public Step mergePaymentDataStep(
			SystemCommandTasklet mergePaymentData) {
		return processPaymentsStepBuilderFactory
			.get("mergePaymentData")
			.tasklet(mergePaymentData("",""))
			.build();
	}

	//------------------------------------------------------------
	// Step 6 - apply payments 
	//------------------------------------------------------------

	@Bean 
	@StepScope 
	public ApplyPayments applyPaymentsTasklet( 
			@Value("#{jobParameters['PMTMRG'] ?: 'data/merged-payment-data.txt'}") String mergedPaymentData,
			@Value("#{jobParameters['PMTSUM'] ?: 'data/payment-summary-data.txt'}") String paymentSummaryData,
			@Value("#{jobParameters['PMTERR'] ?: 'data/payment-error-data.txt'}") String paymentErrorData) {
	    return new ApplyPayments(mergedPaymentData, paymentSummaryData, paymentErrorData);	
	}
	
	
	@Bean 
	public Step applyPaymentsStep(
			ApplyPayments applyPaymentsTasklet) {
		return processPaymentsStepBuilderFactory 
            .get("applyPaymentsTasklet")
            .tasklet(applyPaymentsTasklet("","",""))
            .build();
	}
	
	
	//------------------------------------------------------------
	// Job configuration
	//------------------------------------------------------------

	@Bean
	public Job processPaymentsJob(
			ProcessPaymentsJobCompletionNotificationListener processPaymentsListener) throws Exception {
		return processPaymentsJobBuilderFactory.get("processPaymentsJob")
			.incrementer(new RunIdIncrementer())
			.listener(processPaymentsListener)
			.start(formatPaymentDataFromSource1Step(
					paymentDataFromSource1ItemReader(""), 
					paymentDataFromSource1ItemProcessor(), 
					formattedPaymentDataFromSource1ItemWriter("")))
			.next(sortPaymentDataFromSource1Step(
					sortPaymentDataFromSource1("","")))
			.next(formatPaymentDataFromSource2Step(
					paymentDataFromSource2ItemReader(""), 
					paymentDataFromSource2ItemProcessor(), 
					formattedPaymentDataFromSource2ItemWriter("")))
			.next(sortPaymentDataFromSource2Step(
					sortPaymentDataFromSource2("","")))
			.next(mergePaymentDataStep(
					mergePaymentData("","")))
			.next(applyPaymentsStep(
					applyPaymentsTasklet("","","")))
			.build();
	}
}
