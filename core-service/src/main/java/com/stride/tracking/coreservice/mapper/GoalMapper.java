package com.stride.tracking.coreservice.mapper;

import com.stride.tracking.coreservice.dto.goal.request.CreateGoalRequest;
import com.stride.tracking.coreservice.dto.goal.response.GoalHistoryResponse;
import com.stride.tracking.coreservice.dto.goal.response.GoalResponse;
import com.stride.tracking.coreservice.dto.sport.response.SportShortResponse;
import com.stride.tracking.coreservice.model.Goal;
import com.stride.tracking.coreservice.model.Sport;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GoalMapper {
    public Goal mapToModel(CreateGoalRequest request, Sport sport, String userId) {
        return Goal.builder()
                .sport(sport)
                .userId(userId)
                .type(request.getType())
                .timeFrame(request.getTimeFrame())
                .amount(request.getAmount())
                .active(true)
                .build();
    }

    public GoalResponse mapToResponse(
            Goal goal,
            SportShortResponse sport,
            Long amountGain,
            List<GoalHistoryResponse> histories
    ) {
        return GoalResponse.builder()
                .id(goal.getId())
                .sport(sport)
                .type(goal.getType())
                .timeFrame(goal.getTimeFrame())
                .amountGain(amountGain)
                .amountGoal(goal.getAmount())
                .isActive(goal.isActive())
                .histories(histories)
                .build();
    }
}
