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
public class SportCacheService {
    private final CoreFeignClient coreFeignClient;

    private final SportCacheMapper sportCacheMapper;

    private final TaskScheduler taskScheduler;

    private static final int RETRY_DELAY_MINUTES = 1;

    private final Cache<String, SportCache> sportCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(1000)
            .build();

    @Scheduled(fixedRate = 2700000) //45 minutes
    public void syncSports() {
        try {
            ResponseEntity<SimpleListResponse<SportShortResponse>> response = coreFeignClient.getAllSports();

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                scheduleRetry();
                return;
            }

            for (SportShortResponse sport : response.getBody().getData()) {
                sportCache.put(sport.getId(), sportCacheMapper.mapToModel(sport));
            }
            log.info("Sync sports success at {}", Instant.now());

        } catch (Exception ex) {
            log.error("Failed to sync sports: {}", ex.getMessage());
            scheduleRetry();
        }
    }

    private void scheduleRetry() {
        log.error("Scheduling retry in {} minute(s)...", RETRY_DELAY_MINUTES);
        taskScheduler.schedule(this::syncSports, Instant.now().plus(Duration.ofMinutes(RETRY_DELAY_MINUTES)));
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
