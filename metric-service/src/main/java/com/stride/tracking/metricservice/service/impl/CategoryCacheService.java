package com.stride.tracking.metricservice.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.core.dto.category.event.CategoryUpdatedEvent;
import com.stride.tracking.metric.dto.category.response.CategoryResponse;
import com.stride.tracking.metricservice.client.CoreFeignClient;
import com.stride.tracking.metricservice.constant.Message;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class CategoryCacheService {
    private final CoreFeignClient coreFeignClient;

    private final Cache<String, String> categoryCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(1000)
            .build();

    @Retry(name = "core-service", fallbackMethod = "syncCategoriesFallback")
    public void syncCategories() {
        ResponseEntity<SimpleListResponse<CategoryResponse>> response = coreFeignClient.getAllCategories();

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.error("Failed to sync categories: response status is {} or body is null", response.getStatusCode());
            return;
        }

        for (CategoryResponse categoryResponse : response.getBody().getData()) {
            categoryCache.put(categoryResponse.getId(), categoryResponse.getName());
        }
    }

    void syncCategoriesFallback(Throwable t) {
        log.error("syncCategories failed, fallback called: {}", t.getMessage());
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
