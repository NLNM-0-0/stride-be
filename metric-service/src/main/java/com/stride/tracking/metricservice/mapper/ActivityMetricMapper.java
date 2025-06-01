package com.stride.tracking.metricservice.mapper;

import com.stride.tracking.metric.dto.activity.event.ActivityCreatedEvent;
import com.stride.tracking.metric.dto.report.response.ActivityDetailReport;
import com.stride.tracking.metricservice.model.ActivityMetric;
import org.springframework.stereotype.Component;

@Component
public class ActivityMetricMapper {
    public ActivityMetric mapToModel(ActivityCreatedEvent event) {
        return ActivityMetric.builder()
                .activityId(event.getActivityId())
                .time(event.getTime())
                .name(event.getName())
                .userId(event.getUserId())
                .sportId(event.getSportId())
                .movingTimeSeconds(event.getMovingTimeSeconds())
                .elevationGain(event.getElevationGain())
                .location(event.getLocation())
                .build();
    }

    public ActivityDetailReport mapToReportDetail(ActivityMetric model) {
        return ActivityDetailReport.builder()
                .id(model.getActivityId())
                .sportId(model.getSportId())
                .time(model.getMovingTimeSeconds())
                .elevationGain(model.getElevationGain())
                .distance(model.getDistance())
                .build();
    }
}
