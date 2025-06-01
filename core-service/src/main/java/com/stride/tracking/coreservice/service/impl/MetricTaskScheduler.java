package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.configuration.kafka.KafkaProducer;
import com.stride.tracking.coreservice.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@RequiredArgsConstructor
@Service
public class MetricTaskScheduler {
    private final ActivityRepository activityRepository;
    private final KafkaProducer kafkaProducer;

    private volatile Instant lastSentTime = Instant.now();

    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional
    public void runSendActivityCreatedMetric() {
        Instant now = Instant.now();

        // Lấy dữ liệu từ lastSentTime đến now
        var activities = activityRepository.findAllByCreatedAtBetween(lastSentTime, now);

        for (var activity : activities) {
            ActivityMetricEvent metricEvent = ActivityMetricEvent.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .sportId(activity.getSportId())
                    .time(activity.getCreatedAt())
                    .elevationGain(activity.getElevationGain())
                    .movingTimeSeconds(activity.getMovingTimeSeconds())
                    .location(activity.getLocation().getCity())
                    .build();

            kafkaProducer.send("ACTIVITY_CREATED", metricEvent);
        }

        lastSentTime = now;
    }
}
