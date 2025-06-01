package com.stride.tracking.metricservice.service;

import com.stride.tracking.metric.dto.user.event.UserCreatedEvent;

public interface UserMetricService extends ReportUserService {
    void saveMetrics(UserCreatedEvent event);
}
