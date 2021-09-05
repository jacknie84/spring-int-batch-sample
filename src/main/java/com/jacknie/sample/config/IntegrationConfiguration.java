package com.jacknie.sample.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.integration.launch.JobLaunchingGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.HttpMethod;
import org.springframework.integration.config.EnableIntegrationManagement;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.http.config.EnableIntegrationGraphController;
import org.springframework.integration.http.dsl.Http;
import org.springframework.integration.scheduling.PollerMetadata;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableIntegrationGraphController
@RequiredArgsConstructor
public class IntegrationConfiguration {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata pollerMetadata() {
        return Pollers.fixedRate(Duration.ofSeconds(1)).get();
    }

    @Bean
    public IntegrationFlow integrationFlow1() {
        return IntegrationFlows.from(Http.inboundGateway("/test")
                        .replyTimeout(Duration.ofSeconds(20).toMillis())
                        .requestPayloadType(RequestBody.class)
                        .requestMapping(spec -> spec.methods(HttpMethod.POST)))
                .transform(new JobLaunchRequestTransformer(jobRegistry))
                .log(LoggingHandler.Level.INFO)
                .channel(channels -> channels.executor("integrationFlow2Channel", new SimpleAsyncTaskExecutor()))
                .get();
    }

    @Bean
    public IntegrationFlow integrationFlow2() {
        return IntegrationFlows.from("integrationFlow2Channel")
                .log(LoggingHandler.Level.INFO)
                .handle(new JobLaunchingGateway(jobLauncher))
                .<JobExecution, Map<String, Object>> transform(source -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("status", source.getStatus());
                    map.put("exitStatus", source.getExitStatus());
                    return map;
                })
                .logAndReply(LoggingHandler.Level.INFO);
    }

}
