package com.stride.tracking.metricservice.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.core.dto.sport.event.SportUpdatedEvent;
import com.stride.tracking.metric.dto.sport.response.SportShortResponse;
import com.stride.tracking.metricservice.client.CoreFeignClient;
import com.stride.tracking.metricservice.constant.Message;
import com.stride.tracking.metricservice.mapper.SportCacheMapper;
import com.stride.tracking.metricservice.model.SportCache;
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
public class SportCacheService {
    private final CoreFeignClient coreFeignClient;

    private final SportCacheMapper sportCacheMapper;

    private final Cache<String, SportCache> sportCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(1000)
            .build();

    @Retry(name = "core-service", fallbackMethod = "syncSportsFallback")
    public void syncSports() {
        ResponseEntity<SimpleListResponse<SportShortResponse>> response = coreFeignClient.getAllSports();

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            String msg = "Failed to syncSports: response status is " + response.getStatusCode() + " or body is null";
            log.error(msg);
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, msg);
        }

        for (SportShortResponse sport : response.getBody().getData()) {
            sportCache.put(sport.getId(), sportCacheMapper.mapToModel(sport));
        }
    }

    public void syncSportsFallback(Throwable t) {
        log.error("syncSports failed, fallback called: {}", t.getMessage());
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
