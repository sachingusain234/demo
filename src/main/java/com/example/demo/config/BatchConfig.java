package com.example.demo.config;

import com.example.demo.Model.Data;
import com.example.demo.Repository.DataRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class BatchConfig{
    @Autowired
    private DataRepository dataRepository;
    @Bean
    public FlatFileItemReader<Data> dataReader() {
        FlatFileItemReader<Data> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/data.csv"));
        itemReader.setName("csv-reader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }
    private LineMapper<Data> lineMapper() {

        DefaultLineMapper<Data> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id","name","marks","country");

        BeanWrapperFieldSetMapper<Data> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Data.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }
    @Bean
    public DataProcessor dataProcessor() {
        return new DataProcessor();
    }
    @Bean
    public ItemWriter<Data> dataWriter() {
        return items -> {
            for (Data item : items) {
                // If the entity might already exist, retrieve the managed instance
                Data managedEntity = dataRepository.findById(item.getId()).orElse(null);
                if (managedEntity == null) {
                    dataRepository.save(item); // new insert
                } else {
                    // only update fields
                    managedEntity.setName(item.getName());
                    managedEntity.setMarks(item.getMarks());
                    managedEntity.setCountry(item.getCountry());
                    // DO NOT touch managedEntity.setVersion(...)
                    dataRepository.save(managedEntity); // update
                }
            }
        };
    }
    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step1", jobRepository)
                .<Data, Data>chunk(10, transactionManager)
                .reader(dataReader())
                .processor(dataProcessor())
                .writer(dataWriter())
                .build();
    }
    @Bean
    public Job importUserJob(JobRepository jobRepository, Step step1) {
        return new JobBuilder("importUserJob", jobRepository)
                .start(step1)
                .build();
    }
}