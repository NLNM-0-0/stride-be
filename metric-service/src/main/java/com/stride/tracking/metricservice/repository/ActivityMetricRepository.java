package com.stride.tracking.metricservice.repository;

import com.stride.tracking.metricservice.model.ActivityMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityMetricRepository extends JpaRepository<ActivityMetric, ActivityMetric.ActivityMetricId> {
}
