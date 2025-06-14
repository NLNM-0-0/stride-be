package com.stride.tracking.coreservice.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stride.tracking.commons.configuration.kafka.KafkaProducerConfig;
import com.stride.tracking.commons.configuration.log.LoggingConfig;
import com.stride.tracking.commons.configuration.metrics.MetricsWebMvcConfig;
import com.stride.tracking.commons.configuration.security.SecurityConfig;
import com.stride.tracking.commons.configuration.task.AsyncConfig;
import com.stride.tracking.commons.exception.GlobalExceptionHandler;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Import({
        SecurityConfig.class,
        LoggingConfig.class,
        KafkaProducerConfig.class,
        GlobalExceptionHandler.class,
        MetricsWebMvcConfig.class,
        AsyncConfig.class
})
@EnableCaching
@EnableFeignClients(basePackages = "com.stride.tracking.coreservice.client")
@EnableScheduling
public class ApplicationConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
