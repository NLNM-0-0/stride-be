package com.stride.tracking.coreservice.dto.progress.request;

import com.stride.tracking.coreservice.constant.ProgressTimeFrame;
import com.stride.tracking.coreservice.constant.ProgressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressFilter {
    private String sportId;
    private ProgressType type;
    private ProgressTimeFrame timeFrame;
}
