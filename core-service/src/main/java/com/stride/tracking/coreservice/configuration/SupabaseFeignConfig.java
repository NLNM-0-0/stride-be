package com.stride.tracking.coreservice.configuration;

import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class SupabaseFeignConfig {

    @Bean
    public Request.Options options() {
        return new Request.Options(
                10, TimeUnit.SECONDS,
                60, TimeUnit.SECONDS,
                true
        );
    }
}
