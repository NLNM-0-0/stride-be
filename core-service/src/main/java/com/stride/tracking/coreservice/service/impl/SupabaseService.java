package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.utils.FeignClientHandler;
import com.stride.tracking.core.dto.supabase.request.FindDistrictsContainGeometryRequest;
import com.stride.tracking.core.dto.supabase.request.FindDistrictsNearPointRequest;
import com.stride.tracking.core.dto.supabase.request.FindNearestWayPointsRequest;
import com.stride.tracking.core.dto.supabase.request.GetLocationByGeometryRequest;
import com.stride.tracking.core.dto.supabase.response.FindDistrictsContainGeometryResponse;
import com.stride.tracking.core.dto.supabase.response.FindDistrictsNearPointResponse;
import com.stride.tracking.core.dto.supabase.response.FindNearestWayPointsResponse;
import com.stride.tracking.core.dto.supabase.response.GetLocationByGeometryResponse;
import com.stride.tracking.coreservice.client.SupabaseClient;
import com.stride.tracking.coreservice.constant.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupabaseService {
    private final SupabaseClient supabaseClient;

    @Value("${supabase.access-token}")
    private String accessToken;

    private String getToken() {
        return "Bearer " + accessToken;
    }

    public GetLocationByGeometryResponse getLocationByGeometry(GetLocationByGeometryRequest request) {
        String token = getToken();

        return FeignClientHandler.handleExternalCall(
                () -> supabaseClient.getLocationByGeometry(request, token),
                HttpStatus.INTERNAL_SERVER_ERROR,
                Message.CAN_NOT_GET_LOCATION_FOR_GEOMETRY
        );
    }

    public FindDistrictsNearPointResponse findDistrictsNearPoint(FindDistrictsNearPointRequest request) {
        String token = getToken();

        return FeignClientHandler.handleExternalCall(
                () -> supabaseClient.findDistrictsNearPoint(request, token),
                HttpStatus.INTERNAL_SERVER_ERROR,
                Message.CAN_NOT_FIND_DISTRICTS_NEAR_POINT
        );
    }

    public FindNearestWayPointsResponse findNearestWayPoints(FindNearestWayPointsRequest request) {
        String token = getToken();

        return FeignClientHandler.handleExternalCall(
                () -> supabaseClient.findNearestWayPoints(request, token),
                HttpStatus.INTERNAL_SERVER_ERROR,
                Message.CAN_NOT_FIND_NEAREST_WAY_POINTS
        );
    }

    public FindDistrictsContainGeometryResponse findDistrictsContainGeometry(FindDistrictsContainGeometryRequest request) {
        String token = getToken();

        return FeignClientHandler.handleExternalCall(
                () -> supabaseClient.findDistrictsContainGeometry(request, token),
                HttpStatus.INTERNAL_SERVER_ERROR,
                Message.CAN_NOT_FIND_DISTRICTS_CONTAIN_GEOMETRY
        );
    }
}
