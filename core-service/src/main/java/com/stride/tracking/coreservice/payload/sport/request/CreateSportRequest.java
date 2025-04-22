package com.stride.tracking.coreservice.payload.sport.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSportRequest {
    private String categoryId;
    private String name;
    private String image;
    private List<RuleRequest> rules;
}
