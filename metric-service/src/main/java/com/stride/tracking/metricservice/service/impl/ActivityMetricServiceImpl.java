package com.stride.tracking.metricservice.service.impl;

import com.stride.tracking.dto.route.event.ActivityMetricEvent;
import com.stride.tracking.metricservice.mapper.ActivityMetricMapper;
import com.stride.tracking.metricservice.model.ActivityMetric;
import com.stride.tracking.metricservice.repository.ActivityMetricRepository;
import com.stride.tracking.metricservice.service.ActivityMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityMetricServiceImpl implements ActivityMetricService {
    private final ActivityMetricRepository activityMetricRepository;

    private final ActivityMetricMapper activityMetricMapper;

    public void saveMetrics(ActivityMetricEvent event) {
        ActivityMetric activityMetric = activityMetricMapper.mapToModel(event);
        activityMetricRepository.save(activityMetric);
    }
}
