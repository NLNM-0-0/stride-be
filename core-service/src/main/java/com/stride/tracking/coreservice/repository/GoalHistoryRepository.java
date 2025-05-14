package com.stride.tracking.coreservice.repository;

import com.stride.tracking.coreservice.model.GoalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    @Modifying
    @Transactional
    @Query(value = """
                DELETE FROM core.activity_goal_history 
                WHERE goal_history_id IN (
                    SELECT id FROM core.goal_histories WHERE goal_id = :goalId
                )
            """, nativeQuery = true)
    void deleteLinksToGoalHistories(@Param("goalId") String goalId);
}
