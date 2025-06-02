package com.stride.tracking.profileservice.configuration;

import com.stride.tracking.commons.configuration.kafka.KafkaProducerConfig;
import com.stride.tracking.commons.configuration.log.LoggingConfig;
import com.stride.tracking.commons.configuration.metrics.MetricsConfiguration;
import com.stride.tracking.commons.configuration.security.SecurityConfig;
import com.stride.tracking.commons.exception.GlobalExceptionHandler;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({
        SecurityConfig.class,
        LoggingConfig.class,
        KafkaProducerConfig.class,
        GlobalExceptionHandler.class,
        MetricsConfiguration.class
})
@EnableFeignClients(basePackages = "com.stride.tracking.profileservice.client")
@Configuration
public class ApplicationConfig {
}
