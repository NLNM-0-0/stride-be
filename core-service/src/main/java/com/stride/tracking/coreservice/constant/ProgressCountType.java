package com.stride.tracking.coreservice.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProgressCountType {
    DAILY(1),
    WEEKLY(7),
    ;

    private int countDays;
}
