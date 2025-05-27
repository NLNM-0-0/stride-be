package com.stride.tracking.coreservice.repository;

import com.stride.tracking.coreservice.model.Goal;
import com.stride.tracking.dto.goal.GoalTimeFrame;
import com.stride.tracking.dto.goal.GoalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface GoalRepository  extends JpaRepository<Goal, String>, JpaSpecificationExecutor<Goal> {
    Optional<Goal> findByUserIdAndSportIdAndTypeAndTimeFrame(
            String userId,
            String sportId,
            GoalType type,
            GoalTimeFrame timeFrame
    );

    List<Goal> findByUserId(String userId);

    List<Goal> findBySportId(String sportId);

    List<Goal> findByTimeFrame(GoalTimeFrame timeFrame);
}
