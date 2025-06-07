package com.stride.tracking.metricservice.configuration;

import com.stride.tracking.commons.configuration.log.LoggingConfig;
import com.stride.tracking.commons.configuration.metrics.MetricsWebMvcConfig;
import com.stride.tracking.commons.configuration.security.SecurityConfig;
import com.stride.tracking.commons.configuration.task.AsyncConfig;
import com.stride.tracking.commons.exception.GlobalExceptionHandler;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Import({
        SecurityConfig.class,
        LoggingConfig.class,
        GlobalExceptionHandler.class,
        MetricsWebMvcConfig.class,
        AsyncConfig.class
})
@EnableScheduling
@EnableAsync
@EnableFeignClients(basePackages = "com.stride.tracking.metricservice.client")
@Configuration
public class ApplicationConfig {

}
