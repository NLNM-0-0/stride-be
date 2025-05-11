package com.stride.tracking.coreservice.repository;

import com.stride.tracking.coreservice.model.GoalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface GoalHistoryRepository extends
        JpaRepository<GoalHistory, String>,
        JpaSpecificationExecutor<GoalHistory> {
    List<GoalHistory> findAllByGoalIdAndDateGreaterThanEqual(String goalId, Date date);

    Optional<GoalHistory> findByGoalIdAndDateKey(String goalId, String key);

    void deleteByGoalId(String goalId);

    void deleteAllByCreatedAtBefore(Instant createdAt);
}
