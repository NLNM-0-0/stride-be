package com.stride.tracking.metricservice.controller;

import com.stride.tracking.dto.route.event.ActivityMetricEvent;
import com.stride.tracking.metricservice.service.ActivityMetricService;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityMetricKafkaController {
    private final ActivityMetricService activityMetricService;

    @KafkaListener(topics = "ACTIVITY_CREATED")
    @PermitAll
    public void listenNotificationDelivery(ActivityMetricEvent event) {
        activityMetricService.saveMetrics(event);
    }
}
