package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.coreservice.client.RouteClient;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.dto.route.request.CreateRouteRequest;
import com.stride.tracking.dto.route.request.UpdateRouteRequest;
import com.stride.tracking.dto.route.response.CreateRouteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class RouteService {
    private final RouteClient routeClient;

    public void updateRoute(String routeId, UpdateRouteRequest request) {
        ResponseEntity<SimpleResponse> response = routeClient.update(routeId, request);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.error("[updateRoute] Failed to update route for activity id: {}", request.getActivityId());
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.VIEW_PROFILE_FAILED);
        }

        log.debug("[updateRoute] Success to update route for activity id: {}", request.getActivityId());
    }

    public CreateRouteResponse createRoute(CreateRouteRequest request) {
        ResponseEntity<CreateRouteResponse> response = routeClient.create(request);
        if (response.getStatusCode() != HttpStatus.CREATED || response.getBody() == null) {
            log.error("[createRoute] Failed to create route for activity id: {}", request.getActivityId());
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.VIEW_PROFILE_FAILED);
        }

        log.debug("[createRoute] Success to create route for activity id: {}", request.getActivityId());

        return response.getBody();
    }
}
