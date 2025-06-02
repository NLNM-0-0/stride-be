package com.stride.tracking.metricservice.controller;

import com.stride.tracking.commons.constants.KafkaTopics;
import com.stride.tracking.core.dto.sport.event.SportUpdatedEvent;
import com.stride.tracking.metricservice.service.impl.SportCacheService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SportCacheUpdateKafkaController {
    private final SportCacheService sportCacheService;

    @KafkaListener(topics = KafkaTopics.SPORT_UPDATED_TOPIC)
    @PermitAll
    public void listenSportUpdatedEvent(SportUpdatedEvent event) {
        sportCacheService.updateSport(event);
    }

    @KafkaListener(topics = KafkaTopics.SPORT_CREATED_TOPIC)
    @PermitAll
    public void listenSportCreatedEvent(SportUpdatedEvent event) {
        sportCacheService.updateSport(event);
    }

    @PostConstruct
    public void loadInitialSports() {
        sportCacheService.syncSports();
    }
}
