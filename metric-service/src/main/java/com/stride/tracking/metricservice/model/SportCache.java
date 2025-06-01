package com.stride.tracking.metricservice.model;

import com.stride.tracking.metric.dto.sport.SportMapType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportCache {
    private String id;
    private String name;
    private String image;
    private String color;
    private SportMapType sportMapType;
}
