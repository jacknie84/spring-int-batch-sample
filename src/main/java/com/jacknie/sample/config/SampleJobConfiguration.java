package com.jacknie.sample.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.sql.Date;
import java.time.Instant;

@Configuration
@RequiredArgsConstructor
public class SampleJobConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;

    @Bean
    public Job sampleJob() {
        return jobs.get("sampleJob")
                .incrementer(new RunIdIncrementer())
                .start(sampleStep1())
                .next(sampleStep2())
                .build();
    }

    @Bean
    public Step sampleStep1() {
        new FlowBuilder<SimpleFlow>("complianceSplitFlow").split(new SimpleAsyncTaskExecutor()).add().end();
        return steps.get("sampleStep1")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("sampleStep1 실행");
                    chunkContext.setAttribute("test", "value");
                    chunkContext.getStepContext().setAttribute("test1", "value1");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step sampleStep2() {
        return steps.get("sampleStep2")
                .job(subJob1())
                .parametersExtractor((job, stepExecution) -> new JobParametersBuilder(stepExecution.getJobParameters())
                        .addDate("now", Date.from(Instant.now()), false)
                        .toJobParameters())
                .build();
    }

    @Bean
    public Job subJob1() {
        return jobs.get("subJob1").start(sampleStep3()).build();
    }

    @Bean
    public Step sampleStep3() {
        return steps.get("sampleStep3")
                .tasklet((contribution, chunkContext) -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        //
                    }
                    System.out.println("sampleStep3 실행");
                    System.out.println(chunkContext.getStepContext());
                    System.out.println(chunkContext.getStepContext().getJobExecutionContext());
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

}
