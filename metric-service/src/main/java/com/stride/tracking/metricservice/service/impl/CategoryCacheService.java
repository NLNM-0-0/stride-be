package com.stride.tracking.metricservice.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.core.dto.category.event.CategoryUpdatedEvent;
import com.stride.tracking.metric.dto.category.response.CategoryResponse;
import com.stride.tracking.metricservice.client.CoreFeignClient;
import com.stride.tracking.metricservice.constant.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class CategoryCacheService {
    private final CoreFeignClient coreFeignClient;

    private final TaskScheduler taskScheduler;

    private static final int RETRY_DELAY_MINUTES = 1;

    private final Cache<String, String> categoryCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(1000)
            .build();

    @Scheduled(fixedRate = 2700000) //45 minutes
    public void syncCategories() {
        try {
            ResponseEntity<SimpleListResponse<CategoryResponse>> response = coreFeignClient.getAllCategories();

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                scheduleRetry();
                return;
            }

            for (CategoryResponse categoryResponse : response.getBody().getData()) {
                categoryCache.put(categoryResponse.getId(), categoryResponse.getName());
            }
            log.info("Sync categories success at {}", Instant.now());

        } catch (Exception ex) {
            log.error("Failed to sync categories: {}", ex.getMessage());
            scheduleRetry();
        }
    }

    private void scheduleRetry() {
        log.error("Scheduling retry in {} minute(s)...", RETRY_DELAY_MINUTES);
        taskScheduler.schedule(this::syncCategories, Instant.now().plus(Duration.ofMinutes(RETRY_DELAY_MINUTES)));
    }

    public void updateCategory(CategoryUpdatedEvent event) {
        categoryCache.put(event.getId(), event.getName());
    }

    public Optional<String> getOptionalCategory(String id) {
        return Optional.ofNullable(categoryCache.getIfPresent(id));
    }

    public String getCategory(String id) {
        return getOptionalCategory(id)
                .orElseThrow(() -> new StrideException(HttpStatus.BAD_REQUEST, Message.CATEGORY_IS_NOT_EXISTED));
    }
}
