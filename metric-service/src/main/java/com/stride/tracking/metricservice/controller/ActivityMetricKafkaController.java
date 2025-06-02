package com.stride.tracking.metricservice.controller;

import com.stride.tracking.commons.constants.KafkaTopics;
import com.stride.tracking.metric.dto.activity.event.ActivityCreatedEvent;
import com.stride.tracking.metric.dto.activity.event.ActivityDeletedEvent;
import com.stride.tracking.metric.dto.activity.event.ActivityUpdatedEvent;
import com.stride.tracking.metricservice.service.ActivityMetricService;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityMetricKafkaController {
    private final ActivityMetricService activityMetricService;

    @KafkaListener(topics = KafkaTopics.ACTIVITY_CREATED_TOPIC)
    @PermitAll
    public void listenActivityCreatedEvent(ActivityCreatedEvent event) {
        activityMetricService.saveMetric(event);
    }

    @KafkaListener(topics = KafkaTopics.ACTIVITY_UPDATED_TOPIC)
    @PermitAll
    public void listenActivityUpdatedEvent(ActivityUpdatedEvent event) {
        activityMetricService.updateMetric(event);
    }

    @KafkaListener(topics = KafkaTopics.ACTIVITY_DELETED_TOPIC)
    @PermitAll
    public void listenActivityDeletedEvent(ActivityDeletedEvent event) {
        activityMetricService.deleteMetric(event);
    }
}
