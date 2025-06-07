package com.stride.tracking.metricservice.configuration;

import com.stride.tracking.metricservice.service.impl.CategoryCacheService;
import com.stride.tracking.metricservice.service.impl.SportCacheService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Log4j2
public class ApplicationInit {
    private final SportCacheService sportCacheService;
    private final CategoryCacheService categoryCacheService;

    @PostConstruct
    public void loadInitialSports() {
        CompletableFuture.runAsync(sportCacheService::syncSports);
    }

    @PostConstruct
    public void loadInitialCategories() {
        CompletableFuture.runAsync(categoryCacheService::syncCategories);
    }
}
