package com.stride.tracking.coreservice.repository;

import com.stride.tracking.coreservice.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ActivityRepository extends
        JpaRepository<Activity, String>,
        JpaSpecificationExecutor<Activity> {
}
