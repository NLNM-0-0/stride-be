package com.stride.tracking.coreservice.service;

import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.core.dto.route.request.*;
import com.stride.tracking.core.dto.route.response.CreateRouteResponse;
import com.stride.tracking.core.dto.route.response.RouteResponse;
import com.stride.tracking.core.dto.route.response.SaveRouteResponse;

public interface RouteService {
    SimpleListResponse<RouteResponse> getRecommendedRoutes(GetRecommendRouteRequest request);

    ListResponse<RouteResponse, RouteFilter> getRoutes(AppPageRequest page, RouteFilter filter);

    CreateRouteResponse createRoute(CreateRouteRequest request);

    SaveRouteResponse saveRoute(String routeId, SaveRouteRequest request);

    void updateRoute(String routeId, UpdateRouteRequest request);

    void deleteRoute(String routeId);
}
