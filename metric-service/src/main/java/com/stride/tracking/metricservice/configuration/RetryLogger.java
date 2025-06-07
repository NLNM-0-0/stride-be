package com.stride.tracking.metricservice.configuration;

import io.github.resilience4j.retry.event.RetryOnRetryEvent;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RetryLogger {

    private final RetryRegistry retryRegistry;

    @PostConstruct
    public void registerRetryListeners() {
        retryRegistry.retry("core-service").getEventPublisher()
            .onRetry(this::logRetry);
    }

    private void logRetry(RetryOnRetryEvent event) {
        log.warn("[RETRY] core-service - Attempt #{}, last exception: {}",
            event.getNumberOfRetryAttempts(),
            event.getLastThrowable() != null ? event.getLastThrowable().toString() : "n/a"
        );
    }
}
