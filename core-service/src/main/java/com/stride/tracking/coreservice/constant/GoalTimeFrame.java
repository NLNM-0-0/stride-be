package com.stride.tracking.coreservice.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GoalTimeFrame {
    WEEKLY(12),
    MONTHLY(6),
    ANNUALLY(1)
    ;

    private final int numberHistories;
}
