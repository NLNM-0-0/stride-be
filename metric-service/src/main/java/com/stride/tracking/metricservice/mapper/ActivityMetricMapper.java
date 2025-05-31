package com.stride.tracking.metricservice.mapper;

import com.stride.tracking.dto.route.event.ActivityMetricEvent;
import com.stride.tracking.metricservice.model.ActivityMetric;
import org.springframework.stereotype.Component;

@Component
public class ActivityMetricMapper {
    public ActivityMetric mapToModel(ActivityMetricEvent event) {
        return ActivityMetric.builder()
                .activityId(event.getActivityId())
                .time(event.getTime())
                .userId(event.getUserId())
                .sportId(event.getSportId())
                .movingTimeSeconds(event.getMovingTimeSeconds())
                .elevationGain(event.getElevationGain())
                .location(event.getLocation())
                .build();
    }
}
