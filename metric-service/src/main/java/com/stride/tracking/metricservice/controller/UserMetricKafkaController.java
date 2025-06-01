package com.stride.tracking.metricservice.controller;

import com.stride.tracking.commons.constants.KafkaTopics;
import com.stride.tracking.metric.dto.user.event.UserCreatedEvent;
import com.stride.tracking.metricservice.service.UserMetricService;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMetricKafkaController {
    private final UserMetricService userMetricService;

    @KafkaListener(topics = KafkaTopics.USER_CREATED_TOPIC)
    @PermitAll
    public void listenUserCreatedEvent(UserCreatedEvent event) {
        userMetricService.saveMetrics(event);
    }
}
