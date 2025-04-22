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
public class CreateActivityRequest {
    private String sportId;
    private String name;
    private String description;
    private Double totalDistance;
    private Long movingTimeSeconds;
    private Long elapsedTimeSeconds;
    private Double avgSpeed;
    private List<CoordinateRequest> coordinates;
    private List<Integer> heartRates;
    private List<String> images;
    private int rpe;
}
