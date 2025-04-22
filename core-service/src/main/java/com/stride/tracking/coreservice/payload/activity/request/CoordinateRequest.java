package com.stride.tracking.coreservice.payload.activity.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordinateRequest {
    private List<Double> coordinate;
    private Long timestamp;
}
