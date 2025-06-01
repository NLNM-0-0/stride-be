package com.stride.tracking.metricservice.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.core.dto.sport.event.SportUpdateEvent;
import com.stride.tracking.core.dto.sport.event.SportUpdatedEvent;
import com.stride.tracking.metric.dto.sport.response.SportShortResponse;
import com.stride.tracking.metricservice.client.CoreFeignClient;
import com.stride.tracking.metricservice.constant.Message;
import com.stride.tracking.metricservice.mapper.SportCacheMapper;
import com.stride.tracking.metricservice.model.SportCache;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SportCacheService {
    private final CoreFeignClient coreFeignClient;

    private final SportCacheMapper sportCacheMapper;

    private final Cache<String, SportCache> sportCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(1000)
            .build();

    @Scheduled(fixedRate = 2700000) //45 minutes
    public void syncSports() {
        ResponseEntity<SimpleListResponse<SportShortResponse>> response = coreFeignClient.getAllSport();
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.SYNC_SPORT_CACHE_FAILED);
        }

        for (SportShortResponse sport : response.getBody().getData()) {
            sportCache.put(sport.getId(), sportCacheMapper.mapToModel(sport));
        }
    }

    public void updateSport(SportUpdatedEvent event) {
        sportCache.put(event.getId(), sportCacheMapper.mapToModel(event));
    }

    public Optional<SportCache> getOptionalSport(String id) {
        return Optional.ofNullable(sportCache.getIfPresent(id));
    }

    public SportCache getSport(String id) {
        return getOptionalSport(id)
                .orElseThrow(() -> new StrideException(HttpStatus.BAD_REQUEST, Message.SPORT_IS_NOT_EXISTED));
    }
}
