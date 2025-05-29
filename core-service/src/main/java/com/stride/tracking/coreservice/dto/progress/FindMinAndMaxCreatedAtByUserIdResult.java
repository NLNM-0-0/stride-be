package com.stride.tracking.coreservice.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class FindMinAndMaxCreatedAtByUserIdResult {
    private Instant min;
    private Instant max;
}
