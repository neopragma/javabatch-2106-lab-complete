package com.javabatch.loadempl;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

// tag::setup[]
@Configuration
@EnableBatchProcessing
public class LoadEmplBatchConfiguration {

	@Autowired
	public JobBuilderFactory loadEmplJobBuilderFactory;

	@Autowired
	public StepBuilderFactory loadEmplStepBuilderFactory;
	// end::setup[]

	// tag::readerwriterprocessor[]
	@Bean
	@StepScope
	public FlatFileItemReader<Employee> employeeReader(
			@Value("#{jobParameters['EMPLIN'] ?: 'data/emplin'}") String inputFilePath) {
		FixedLengthTokenizer lineTokenizer = new FixedLengthTokenizer();
		lineTokenizer.setNames("name","hireDateAsYYYYMMDD","ssn", "errorMessage");
		lineTokenizer.setColumns(new Range(1,80), new Range(81, 88), new Range(89, 97), new Range(98, 147));
		
		DefaultLineMapper<Employee> employeeLineMapper = new DefaultLineMapper<>();
		employeeLineMapper.setLineTokenizer(lineTokenizer);
		employeeLineMapper.setFieldSetMapper((FieldSetMapper<Employee>) new EmployeeFieldSetMapper());
		employeeLineMapper.afterPropertiesSet();
		
		return new FlatFileItemReaderBuilder<Employee>()
			.resource(new FileSystemResource(inputFilePath))	
			.encoding("UTF-8")
			.lineMapper(employeeLineMapper)
			.strict(true)
			.name("employeeReader")
			.build();
		}
    
    @Bean
    public ClassifierCompositeItemWriter employeeClassifierCompositeItemWriter() {
    	ClassifierCompositeItemWriter compositeItemWriter = new ClassifierCompositeItemWriter();
		compositeItemWriter.setClassifier((Classifier<Employee, ItemWriter<? super Employee>>) employee -> {	
			final HireDate hireDate;
			try {
				hireDate = new HireDate(employee.getHireDateAsYYYYMMDD());				
			} catch (HireDateException hde) {
				employee.setErrorMessage(hde.getMessage());
				return (ItemWriter<? super Employee>) invalidEmployeeItemWriter("");
			}
			final SSN ssn;
			try {
				ssn = new SSN(employee.getSsn());
			} catch (SsnException ssne) {
				employee.setErrorMessage(ssne.getMessage());
				return (ItemWriter<? super Employee>) invalidEmployeeItemWriter("");
			}
			return (ItemWriter<? super Employee>) validEmployeeItemWriter("");
		});
		return compositeItemWriter;            	
    }
	
	@Bean
	@StepScope
	public FlatFileItemWriter<Employee> validEmployeeItemWriter(
			@Value("#{jobParameters['EMPLVALD'] ?: 'data/emplvald'}") String validOutputFilePath) {
		return new FlatFileItemWriterBuilder<Employee>()
			.resource(new FileSystemResource(validOutputFilePath))
			.lineAggregator(new EmployeeLineAggregator())
			.encoding("UTF-8")
			.name("validEmployeeItemWriter")
			.shouldDeleteIfExists(true)
			.build();
	}
	
	@Bean
	@StepScope
	public FlatFileItemWriter<Employee> invalidEmployeeItemWriter(
			@Value("#{jobParameters['EMPLERR'] ?: 'data/emplerr'}") String invalidOutputFilePath) {
		return new FlatFileItemWriterBuilder<Employee>()
			.resource(new FileSystemResource(invalidOutputFilePath))
			.lineAggregator(new EmployeeWithErrorsLineAggregator())
			.encoding("UTF-8")
			.name("invalidEmployeeItemWriter")
			.shouldDeleteIfExists(true)
			.build();
	}
	// end::readerwriterprocessor[]

	// tag::jobstep[]
	@Bean
	public Job loadEmplJob(LoadEmplJobCompletionNotificationListener loadEmplListener, Step loadEmplStep1) {
		return loadEmplJobBuilderFactory.get("loadEmplJob")
			.incrementer(new RunIdIncrementer())
			.listener(loadEmplListener)
			.flow(loadEmplStep1)
			.end()
			.build();
	}
	
	@Bean
	public SkipPolicy employeeSkipPolicy() {
		return new EmployeeSkipPolicy();
	}


	@Bean
	public Step loadEmplStep1(ClassifierCompositeItemWriter<Employee> employeeClassifierCompositeItemWriter) throws Exception {
		return loadEmplStepBuilderFactory.get("step1")
			.<Employee, Employee> chunk(10)
			.reader(employeeReader(""))
			.writer(employeeClassifierCompositeItemWriter())
			.faultTolerant()
			.skipPolicy(employeeSkipPolicy())
			.stream(validEmployeeItemWriter(""))
			.stream(invalidEmployeeItemWriter(""))
			.build();
	}
	// end::jobstep[]
}
