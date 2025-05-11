package com.stride.tracking.coreservice.service;

import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.coreservice.dto.goal.request.CreateGoalRequest;
import com.stride.tracking.coreservice.dto.goal.request.UpdateGoalRequest;
import com.stride.tracking.coreservice.dto.goal.response.CreateGoalResponse;
import com.stride.tracking.coreservice.dto.goal.response.GoalResponse;

public interface GoalService {
    SimpleListResponse<GoalResponse> getUserGoals();

    CreateGoalResponse createGoal(CreateGoalRequest request);

    void updateGoal(String id, UpdateGoalRequest request);

    void deleteGoal(String id);
}
