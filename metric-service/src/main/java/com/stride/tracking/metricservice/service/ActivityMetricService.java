package com.stride.tracking.metricservice.service;

import com.stride.tracking.dto.route.event.ActivityMetricEvent;

public interface ActivityMetricService {
    void saveMetrics(ActivityMetricEvent event);
}
