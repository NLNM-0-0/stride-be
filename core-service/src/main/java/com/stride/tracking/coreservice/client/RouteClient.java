package com.stride.tracking.coreservice.client;

import com.stride.tracking.commons.configuration.feign.FeignConfig;
import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.dto.route.request.CreateRouteRequest;
import com.stride.tracking.dto.route.request.UpdateRouteRequest;
import com.stride.tracking.dto.route.response.CreateRouteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "route-service",
        url = "${app.services.route}",
        configuration = FeignConfig.class
)
@Component
public interface RouteClient {
    @PostMapping(value = "/stride-routes", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CreateRouteResponse> create(@RequestBody CreateRouteRequest request);

    @PutMapping(value = "/stride-routes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<SimpleResponse> update(
            @PathVariable String id,
            @RequestBody UpdateRouteRequest request
    );
}
