package com.stride.tracking.coreservice.payload.sport.response;

import com.stride.tracking.coreservice.payload.category.response.CategoryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportResponse {
    private String id;
    private CategoryResponse category;
    private String name;
    private String image;
    private List<RuleResponse> rules;
}
