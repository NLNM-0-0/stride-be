package com.stride.tracking.coreservice.payload.sport.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleRequest {
    private String expression;
    private Double met;
}
