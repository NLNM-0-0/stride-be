package com.stride.tracking.coreservice.payload.elevation.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequest {
    private Double longitude;
    private Double latitude;
}
