package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.utils.FeignClientHandler;
import com.stride.tracking.core.dto.elevation.request.ElevationRequest;
import com.stride.tracking.core.dto.elevation.request.LocationRequest;
import com.stride.tracking.core.dto.elevation.response.ElevationResponse;
import com.stride.tracking.coreservice.client.OpenElevationFeignClient;
import com.stride.tracking.coreservice.constant.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class ElevationService {
    private final OpenElevationFeignClient elevationClient;

    public List<Integer> calculateElevations(List<List<Double>> coordinates) {
        List<LocationRequest> locationRequests = coordinates.stream()
                .map(coordinate -> {
                    Double longitude = coordinate.get(0);
                    Double latitude = coordinate.get(1);
                    return LocationRequest.builder()
                            .longitude(longitude)
                            .latitude(latitude)
                            .build();
                })
                .toList();

        ElevationRequest request = ElevationRequest.builder()
                .locations(locationRequests)
                .build();

        ElevationResponse response = FeignClientHandler.handleExternalCall(
                ()->elevationClient.calculateElevation(request),
                HttpStatus.INTERNAL_SERVER_ERROR,
                Message.CALCULATE_ELEVATIONS_FAILED
        );

        return response.getResults().stream()
                .map(location -> location.getElevation().intValue())
                .toList();
    }
}
