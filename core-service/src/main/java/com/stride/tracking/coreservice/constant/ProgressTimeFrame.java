package com.stride.tracking.coreservice.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProgressTimeFrame {
    WEEK("7D", ProgressCountType.DAILY),
    MONTH("1M", ProgressCountType.DAILY),
    THREE_MONTHS("3M", ProgressCountType.WEEKLY),
    SIX_MONTHS("6M", ProgressCountType.WEEKLY),
    YEAR_TO_DATE("YTD", ProgressCountType.WEEKLY),
    YEAR("1Y", ProgressCountType.WEEKLY)
    ;

    private final String name;
    private final ProgressCountType countType;
}
