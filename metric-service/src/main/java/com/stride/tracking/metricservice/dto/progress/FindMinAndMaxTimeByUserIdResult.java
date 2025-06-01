package com.stride.tracking.metricservice.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindMinAndMaxTimeByUserIdResult {
    private Instant min;
    private Instant max;
}
