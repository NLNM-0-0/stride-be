package com.stride.tracking.coreservice.controller;

import com.stride.tracking.commons.annotations.PreAuthorizeUser;
import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.core.dto.route.request.*;
import com.stride.tracking.core.dto.route.response.CreateRouteResponse;
import com.stride.tracking.core.dto.route.response.RouteResponse;
import com.stride.tracking.core.dto.route.response.SaveRouteResponse;
import com.stride.tracking.coreservice.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PostMapping("/recommend")
    @PreAuthorizeUser
    public ResponseEntity<SimpleListResponse<RouteResponse>> getRecommendedRoutes(
            @RequestBody GetRecommendRouteRequest request
    ) {
        return ResponseEntity.ok(routeService.getRecommendedRoutes(request));
    }

    @GetMapping("/profile")
    @PreAuthorizeUser
    public ResponseEntity<ListResponse<RouteResponse, RouteFilter>> getUserRoute(
            @Valid AppPageRequest page,
            @Valid RouteFilter filter
    ) {
        return ResponseEntity.ok(routeService.getRoutes(page, filter));
    }

    @PostMapping("")
    @PreAuthorizeUser
    public ResponseEntity<CreateRouteResponse> createRoute(
            @RequestBody CreateRouteRequest request
    ) {
        CreateRouteResponse response = routeService.createRoute(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{routeId}/save")
    @PreAuthorizeUser
    public ResponseEntity<SaveRouteResponse> saveRoute(
            @PathVariable String routeId,
            @RequestBody SaveRouteRequest request
    ) {
        SaveRouteResponse response = routeService.saveRoute(routeId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{routeId}")
    @PreAuthorizeUser
    public ResponseEntity<SimpleResponse> updateRoute(
            @PathVariable String routeId,
            @RequestBody UpdateRouteRequest request
    ) {
        routeService.updateRoute(routeId, request);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @DeleteMapping("/{routeId}")
    @PreAuthorizeUser
    public ResponseEntity<SimpleResponse> deleteRoute(
            @PathVariable String routeId
    ) {
        routeService.deleteRoute(routeId);
        return ResponseEntity.ok(new SimpleResponse());
    }
}
