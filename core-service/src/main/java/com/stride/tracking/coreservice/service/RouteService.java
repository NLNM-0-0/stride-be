package com.stride.tracking.coreservice.service;

import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.dto.route.request.GetRecommendRouteRequest;
import com.stride.tracking.dto.route.request.RouteFilter;
import com.stride.tracking.dto.route.request.SaveRouteRequest;
import com.stride.tracking.dto.route.request.UpdateRouteRequest;
import com.stride.tracking.dto.route.response.RouteResponse;
import com.stride.tracking.dto.route.response.SaveRouteResponse;
import com.stride.tracking.dto.route.request.CreateRouteRequest;
import com.stride.tracking.dto.route.response.CreateRouteResponse;

public interface RouteService {
    SimpleListResponse<RouteResponse> getRecommendedRoutes(GetRecommendRouteRequest request);

    ListResponse<RouteResponse, RouteFilter> getRoutes(AppPageRequest page, RouteFilter filter);

    CreateRouteResponse createRoute(CreateRouteRequest request);

    SaveRouteResponse saveRoute(String routeId, SaveRouteRequest request);

    void updateRoute(String routeId, UpdateRouteRequest request);

    void deleteRoute(String routeId);
}
