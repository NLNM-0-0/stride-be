package com.stride.tracking.coreservice.dto.goal.response;

import com.stride.tracking.coreservice.constant.GoalTimeFrame;
import com.stride.tracking.coreservice.constant.GoalType;
import com.stride.tracking.coreservice.dto.sport.response.SportShortResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {
    private String id;
    private SportShortResponse sport;
    private GoalType type;
    private GoalTimeFrame timeFrame;
    private Long amountGain;
    private Long amountGoal;
    private Boolean isActive;
    private List<GoalHistoryResponse> histories;
}
