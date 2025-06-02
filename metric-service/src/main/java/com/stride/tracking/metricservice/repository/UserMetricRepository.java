package com.stride.tracking.metricservice.repository;

import com.stride.tracking.metricservice.model.UserMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface UserMetricRepository extends JpaRepository<UserMetric, UserMetric.UserMetricId> {
    List<UserMetric> findAllByTimeBetween(
            Instant from,
            Instant to
    );
}
