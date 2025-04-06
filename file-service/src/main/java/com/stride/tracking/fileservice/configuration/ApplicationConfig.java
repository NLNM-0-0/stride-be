package com.stride.tracking.fileservice.configuration;

import com.stride.tracking.commons.security.SecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({SecurityConfig.class})
@Configuration
public class ApplicationConfig {
}
