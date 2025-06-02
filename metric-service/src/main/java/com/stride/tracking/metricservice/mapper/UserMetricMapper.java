package com.stride.tracking.metricservice.mapper;

import com.stride.tracking.metric.dto.report.AuthProvider;
import com.stride.tracking.metric.dto.user.event.UserCreatedEvent;
import com.stride.tracking.metricservice.model.UserMetric;
import org.springframework.stereotype.Component;

@Component
public class UserMetricMapper {
    public UserMetric mapToModel(UserCreatedEvent event) {
        return UserMetric.builder()
                .userId(event.getUserId())
                .time(event.getTime())
                .provider(AuthProvider.valueOf(event.getProvider()))
                .build();
    }
}
