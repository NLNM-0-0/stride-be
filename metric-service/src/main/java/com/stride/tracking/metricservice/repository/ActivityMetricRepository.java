package com.stride.tracking.metricservice.repository;

import com.stride.tracking.metricservice.dto.progress.FindMinAndMaxTimeByUserIdResult;
import com.stride.tracking.metricservice.model.ActivityMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ActivityMetricRepository extends JpaRepository<ActivityMetric, ActivityMetric.ActivityMetricId> {
    List<ActivityMetric> findAllByTimeBetween(
            Instant from,
            Instant to
    );

    void deleteByActivityId(String activityId);

    List<ActivityMetric> findAllByUserIdAndSportIdAndTimeGreaterThanEqual(
            String userId,
            String sportId,
            Instant createdAt
    );

    List<ActivityMetric> findAllByUserIdAndTimeGreaterThanEqualOrderByTimeDesc(
            String userId,
            Instant createdAt
    );

    List<ActivityMetric> findAllByUserIdAndSportIdAndTimeGreaterThanEqualAndTimeLessThanEqual(
            String userId,
            String sportId,
            Instant fromDate,
            Instant toDate
    );

    List<ActivityMetric> findAllByUserIdAndSportIdInAndTimeGreaterThanEqualAndTimeLessThanEqual(
            String userId,
            List<String> sportId,
            Instant fromDate,
            Instant toDate
    );

    List<ActivityMetric> findAllByUserIdAndTimeGreaterThanEqualAndTimeLessThanEqual(
            String userId,
            Instant fromDate,
            Instant toDate
    );

    @Query("""
        SELECT am.sportId
        FROM activity_metrics am
        WHERE am.userId = :userId
        AND am.time >= :fromDate
        GROUP BY am.sportId
    """)
    List<String> findDistinctSportsSinceNative(
            @Param("userId") String userId,
            @Param("fromDate") Instant fromDate
    );

    @Query("""
        SELECT new com.stride.tracking.metricservice.dto.progress.FindMinAndMaxTimeByUserIdResult(
            MIN(am.time), MAX(am.time)
        )
        FROM activity_metrics am
        WHERE am.userId = :userId
    """)
    FindMinAndMaxTimeByUserIdResult findMinAndMaxTimeByUserId(@Param("userId") String userId);

    Optional<ActivityMetric> findByActivityId(String activityId);

    void deleteByTimeBefore(Instant createdAt);
}
