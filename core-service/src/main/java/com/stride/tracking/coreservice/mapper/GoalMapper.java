package com.stride.tracking.coreservice.mapper;

import com.stride.tracking.coreservice.model.Goal;
import com.stride.tracking.coreservice.model.Sport;
import com.stride.tracking.dto.goal.request.CreateGoalRequest;
import com.stride.tracking.dto.goal.response.GoalHistoryResponse;
import com.stride.tracking.dto.goal.response.GoalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GoalMapper {
    private final SportMapper sportMapper;

    public Goal mapToModel(
            CreateGoalRequest request,
            Sport sport,
            String userId
    ) {
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
            Long amountGain,
            List<GoalHistoryResponse> histories
    ) {
        return GoalResponse.builder()
                .id(goal.getId())
                .sport(sportMapper.mapToShortResponse(goal.getSport()))
                .type(goal.getType())
                .timeFrame(goal.getTimeFrame())
                .amountGain(amountGain)
                .amountGoal(goal.getAmount())
                .isActive(goal.isActive())
                .histories(histories)
                .build();
    }
}
