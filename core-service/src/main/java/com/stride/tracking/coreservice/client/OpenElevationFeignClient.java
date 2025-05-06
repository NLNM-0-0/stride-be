package com.stride.tracking.coreservice.client;

import com.stride.tracking.dto.elevation.request.ElevationRequest;
import com.stride.tracking.dto.elevation.response.ElevationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "open-elevation",
        url = "${app.services.open-elevation}"
)
@Component
public interface OpenElevationFeignClient {
    @PostMapping(value = "/api/v1/lookup", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ElevationResponse> calculateElevation(@RequestBody ElevationRequest elevationRequest);
}
