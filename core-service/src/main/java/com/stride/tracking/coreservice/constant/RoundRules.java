package com.stride.tracking.coreservice.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoundRules {
    DISTANCE(2),
    CARBON_SAVED(1),
    SPEED(5),
    HEART_RATE(1)
    ;

    private final int value;
}
