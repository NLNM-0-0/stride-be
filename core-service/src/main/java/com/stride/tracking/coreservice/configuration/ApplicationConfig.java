package com.stride.tracking.coreservice.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stride.tracking.commons.configuration.kafka.KafkaProducerConfig;
import com.stride.tracking.commons.configuration.log.LoggingConfig;
import com.stride.tracking.commons.configuration.metrics.MetricsConfiguration;
import com.stride.tracking.commons.configuration.security.SecurityConfig;
import com.stride.tracking.commons.configuration.task.AsyncConfig;
import com.stride.tracking.commons.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        SecurityConfig.class,
        LoggingConfig.class,
        KafkaProducerConfig.class,
        GlobalExceptionHandler.class,
        MetricsConfiguration.class,
        AsyncConfig.class
})
public class ApplicationConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
