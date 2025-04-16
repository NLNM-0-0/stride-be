package com.stride.tracking.profileservice.configuration;

import com.stride.tracking.commons.configuration.log.LoggingConfig;
import com.stride.tracking.commons.configuration.security.SecurityConfig;
import com.stride.tracking.commons.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({
        SecurityConfig.class,
        LoggingConfig.class,
        GlobalExceptionHandler.class})
@Configuration
public class ApplicationConfig {
}
