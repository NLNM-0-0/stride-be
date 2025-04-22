package com.stride.tracking.coreservice.payload.elevation.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {
    private Double longitude;
    private Double latitude;
    private Double elevation;
}
