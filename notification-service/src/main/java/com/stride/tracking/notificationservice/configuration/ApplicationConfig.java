package com.stride.tracking.notificationservice.configuration;

import com.stride.tracking.commons.exception.GlobalExceptionHandler;
import com.stride.tracking.commons.security.SecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@EnableAsync
@Configuration
public class ApplicationConfig {

}
