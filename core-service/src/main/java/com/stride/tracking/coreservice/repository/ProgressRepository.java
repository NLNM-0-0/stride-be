package com.stride.tracking.coreservice.repository;

import com.stride.tracking.coreservice.dto.progress.FindMinAndMaxCreatedAtByUserIdResult;
import com.stride.tracking.coreservice.model.Progress;
import com.stride.tracking.coreservice.model.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ProgressRepository extends JpaRepository<Progress, String>, JpaSpecificationExecutor<Progress> {
    void deleteByActivity_Id(String activityId);

    List<Progress> findAllByUserIdAndSportAndCreatedAtGreaterThanEqual(
            String userId,
            Sport sport,
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

    List<Progress> findAllByUserIdAndSport_IdInAndCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(
            String userId,
            List<String> sportId,
            Instant fromDate,
            Instant toDate
    );

    List<Progress> findAllByUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(
            String userId,
            Instant fromDate,
            Instant toDate
    );

    @Query("""
        SELECT s
        FROM progress p
        JOIN p.sport s
        WHERE p.userId = :userId
        AND p.createdAt >= :fromDate
        GROUP BY s.id
    """)
    List<Sport> findDistinctSportsSinceNative(
            @Param("userId") String userId,
            @Param("fromDate") Instant fromDate
    );

    @Query("""
        SELECT new com.stride.tracking.coreservice.dto.progress.FindMinAndMaxCreatedAtByUserIdResult(
            MIN(p.createdAt), MAX(p.createdAt)
        )
        FROM progress p
        WHERE p.userId = :userId
    """)
    FindMinAndMaxCreatedAtByUserIdResult findMinAndMaxCreatedAtByUserId(@Param("userId") String userId);
}
