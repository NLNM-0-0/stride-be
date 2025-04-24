package com.stride.tracking.coreservice.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule {
    private String expression;
    private Double met;
}
