package com.stride.tracking.coreservice.repository;

import com.stride.tracking.coreservice.model.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;

public interface ProgressRepository extends JpaRepository<Progress, String>, JpaSpecificationExecutor<Progress> {
    void deleteByActivity_Id(String activityId);

    List<Progress> findAllByUserIdAndSport_IdAndCreatedAtGreaterThanEqual(
            String userId,
            String sportId,
            Instant createdAt
    );

    List<Progress> findAllByUserIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            String userId,
            Instant createdAt
    );

    List<Progress> findAllByUserIdAndSport_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(
            String userId,
            String sportId,
            Instant fromDate,
            Instant toDate
    );
}
