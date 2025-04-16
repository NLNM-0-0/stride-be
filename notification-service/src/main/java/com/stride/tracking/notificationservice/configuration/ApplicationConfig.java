package com.stride.tracking.notificationservice.configuration;

import com.stride.tracking.commons.configuration.log.LoggingConfig;
import com.stride.tracking.commons.configuration.security.SecurityConfig;
import com.stride.tracking.commons.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@Import({
        SecurityConfig.class,
        LoggingConfig.class,
        GlobalExceptionHandler.class})
@EnableAsync
@Configuration
public class ApplicationConfig {

}
