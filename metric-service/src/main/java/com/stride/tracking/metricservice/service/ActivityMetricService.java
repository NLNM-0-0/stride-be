package com.stride.tracking.metricservice.service;

import com.stride.tracking.metric.dto.activity.event.ActivityCreatedEvent;
import com.stride.tracking.metric.dto.activity.event.ActivityDeletedEvent;
import com.stride.tracking.metric.dto.activity.event.ActivityUpdatedEvent;

public interface ActivityMetricService extends ReportActivityService, ReportSportService {
    void saveMetric(ActivityCreatedEvent event);

    void updateMetric(ActivityUpdatedEvent event);

    void deleteMetric(ActivityDeletedEvent event);
}
