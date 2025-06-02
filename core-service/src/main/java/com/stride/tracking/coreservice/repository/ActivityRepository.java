package com.stride.tracking.coreservice.repository;

import com.stride.tracking.coreservice.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;

public interface ActivityRepository extends
        JpaRepository<Activity, String>,
        JpaSpecificationExecutor<Activity> {
    List<Activity> findAllByCreatedAtBetween(Instant from, Instant to);
}
