package com.jacknie.sample.config;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.integration.transformer.GenericTransformer;

import java.util.UUID;

@RequiredArgsConstructor
public class JobLaunchRequestTransformer implements GenericTransformer<RequestBody, JobLaunchRequest> {

    private final JobRegistry jobRegistry;

    @SneakyThrows
    @Override
    public JobLaunchRequest transform(RequestBody source) {
        Job job = jobRegistry.getJob("sampleJob");
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("job.request.id", UUID.randomUUID().toString())
                .toJobParameters();
        return new JobLaunchRequest(job, jobParameters);
    }

}
