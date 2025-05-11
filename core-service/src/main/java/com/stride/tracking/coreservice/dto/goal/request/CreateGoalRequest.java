package com.stride.tracking.coreservice.dto.goal.request;

import com.stride.tracking.coreservice.constant.GoalTimeFrame;
import com.stride.tracking.coreservice.constant.GoalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGoalRequest {
    private String sportId;
    private GoalType type;
    private GoalTimeFrame timeFrame;
    private Long amount;
}
