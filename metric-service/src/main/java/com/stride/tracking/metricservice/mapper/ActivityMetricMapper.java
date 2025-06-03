package com.stride.tracking.metricservice.mapper;

import com.stride.tracking.metric.dto.activity.event.ActivityCreatedEvent;
import com.stride.tracking.metric.dto.report.response.activity.ActivityDetailReport;
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
                .calories(event.getCalories())
                .avgHeartRate(event.getAvgHearRate())
                .build();
    }

    public ActivityDetailReport mapToReportDetail(ActivityMetric model) {
        return ActivityDetailReport.builder()
                .id(model.getActivityId())
                .sportId(model.getSportId())
                .time(model.getMovingTimeSeconds())
                .elevationGain(model.getElevationGain())
                .distance(model.getDistance())
                .avgHeartRate(model.getAvgHeartRate())
                .calories(model.getCalories())
                .build();
    }
}
