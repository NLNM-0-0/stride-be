package com.stride.tracking.coreservice.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindMinAndMaxCreatedAtByUserIdResult {
    private Instant min;
    private Instant max;
}
