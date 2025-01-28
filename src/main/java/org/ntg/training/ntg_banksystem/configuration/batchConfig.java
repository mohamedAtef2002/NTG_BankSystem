package org.ntg.training.ntg_banksystem.configuration;

import lombok.RequiredArgsConstructor;
import org.ntg.training.ntg_banksystem.entity.Account;
import org.ntg.training.ntg_banksystem.entity.Customer;
import org.ntg.training.ntg_banksystem.entity.Transaction;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class batchConfig {

    //step1
    private final customerReader customerReader;
    private final customerWriter customerWriter;

    //step2
    private final transactionReader transactionReader;
    private final transactionWriter transactionWriter;

    @Bean
    public Job bankJob(JobRepository jobRepository, Step step1, Step step2) {
        return new JobBuilder("bankJob",jobRepository)
                .start(step1)
                .next(step2)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step1", jobRepository)
                .<Customer, Customer> chunk(100, transactionManager)
                .reader(customerReader)
                .writer(customerWriter)
                .build();
    }

    @Bean
    public Step step2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step2", jobRepository)
                .<Transaction, Transaction> chunk(100, transactionManager)
                .reader(transactionReader)
                .writer(transactionWriter)
                .build();
    }

}
