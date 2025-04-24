package com.stride.tracking.bridgeservice.configuration;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OkHttpClientProvider {
    private static final OkHttpClient client = new OkHttpClient();

    @Bean
    public OkHttpClient getOkHttpClient() {
        return client;
    }
}