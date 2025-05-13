package com.stride.tracking.coreservice.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.stride.tracking.coreservice.constant.CacheName;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheManagerConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                CacheName.CATEGORY_BY_ID,
                CacheName.SPORT_BY_ID
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(1000));
        return cacheManager;
    }
}
