package com.stride.tracking.bridgeservice.configuration;

import com.stride.tracking.commons.configuration.log.LoggingConfig;
import com.stride.tracking.commons.configuration.metrics.MetricsConfiguration;
import com.stride.tracking.commons.configuration.security.SecurityConfig;
import com.stride.tracking.commons.configuration.task.AsyncConfig;
import com.stride.tracking.commons.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@Import({
        SecurityConfig.class,
        LoggingConfig.class,
        GlobalExceptionHandler.class,
        MetricsConfiguration.class,
        AsyncConfig.class
})
@EnableAsync
@Configuration
public class ApplicationConfig {

}
