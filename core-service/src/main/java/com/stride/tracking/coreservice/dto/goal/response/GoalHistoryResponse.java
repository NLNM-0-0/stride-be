package com.stride.tracking.coreservice.dto.goal.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalHistoryResponse {
    private String key;
    private Long amountGain;
    private Long amountGoal;
}
