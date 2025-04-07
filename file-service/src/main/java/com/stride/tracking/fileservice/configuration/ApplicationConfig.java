package com.stride.tracking.fileservice.configuration;

import com.stride.tracking.commons.exception.GlobalExceptionHandler;
import com.stride.tracking.commons.security.SecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@Configuration
public class ApplicationConfig {
}
