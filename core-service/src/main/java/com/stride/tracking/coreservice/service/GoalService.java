package com.stride.tracking.coreservice.service;

import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.dto.goal.request.CreateGoalRequest;
import com.stride.tracking.dto.goal.request.UpdateGoalRequest;
import com.stride.tracking.dto.goal.response.CreateGoalResponse;
import com.stride.tracking.dto.goal.response.GoalResponse;

import java.time.ZoneId;

public interface GoalService {
    SimpleListResponse<GoalResponse> getUserGoals(ZoneId zoneId);

    CreateGoalResponse createGoal(CreateGoalRequest request);

    void updateGoal(String id, ZoneId zoneId, UpdateGoalRequest request);

    void deleteGoal(String id);
}
