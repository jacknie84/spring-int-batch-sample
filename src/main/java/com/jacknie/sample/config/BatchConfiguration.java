package com.jacknie.sample.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.ApplicationContextFactory;
import org.springframework.batch.core.configuration.support.GenericApplicationContextFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing(modular = true)
public class BatchConfiguration {

    @Bean
    public ApplicationContextFactory applicationContextFactory() {
        return new GenericApplicationContextFactory(SampleJobConfiguration.class);
    }

}
