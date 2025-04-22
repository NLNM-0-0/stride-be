package com.stride.tracking.coreservice.payload.sport.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleResponse {
    private String expression;
    private Double met;
}
