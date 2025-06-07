package com.stride.tracking.metricservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheScheduler {
    private final SportCacheService sportCacheService;
    private final CategoryCacheService categoryCacheService;

    @Scheduled(fixedRate = 2700000)
    public void syncSports() {
        sportCacheService.syncSports();
    }

    @Scheduled(fixedRate = 2700000)
    public void syncCategories() {
        categoryCacheService.syncCategories();
    }
}
