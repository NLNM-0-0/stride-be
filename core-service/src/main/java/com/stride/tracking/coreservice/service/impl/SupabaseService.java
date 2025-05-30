package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.coreservice.client.SupabaseClient;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.dto.supabase.request.FindDistrictsContainGeometryRequest;
import com.stride.tracking.dto.supabase.request.FindDistrictsNearPointRequest;
import com.stride.tracking.dto.supabase.request.FindNearestWayPointsRequest;
import com.stride.tracking.dto.supabase.request.GetLocationByGeometryRequest;
import com.stride.tracking.dto.supabase.response.FindDistrictsContainGeometryResponse;
import com.stride.tracking.dto.supabase.response.FindDistrictsNearPointResponse;
import com.stride.tracking.dto.supabase.response.GetLocationByGeometryResponse;
import com.stride.tracking.dto.supabase.response.FindNearestWayPointsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        ResponseEntity<GetLocationByGeometryResponse> response = supabaseClient.getLocationByGeometry(request, token);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.error("[getLocationByGeometry] Failed to get location for geometry: {}", request.getGeometry());
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.CAN_NOT_GET_LOCATION_FOR_GEOMETRY);
        }

        return response.getBody();
    }

    public FindDistrictsNearPointResponse findDistrictsNearPoint(FindDistrictsNearPointRequest request) {
        String token = getToken();
        ResponseEntity<FindDistrictsNearPointResponse> response = supabaseClient.findDistrictsNearPoint(request, token);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.error(
                    "[findDistrictsNearPoint] Failed to find districts near point: ({}, {}) around {}",
                    request.getLat(),
                    request.getLon(),
                    request.getAround()
            );
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.CAN_NOT_FIND_DISTRICTS_NEAR_POINT);
        }

        return response.getBody();
    }

    public FindNearestWayPointsResponse findNearestWayPoints(FindNearestWayPointsRequest request) {
        String token = getToken();
        ResponseEntity<FindNearestWayPointsResponse> response = supabaseClient.findNearestWayPoints(request, token);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.error(
                    "[findNearestWayPoints] Failed to find nearest way points type {} of {}",
                    request.getType(),
                    request.getData()
            );
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.CAN_NOT_FIND_NEAREST_WAY_POINTS);
        }

        return response.getBody();
    }

    public FindDistrictsContainGeometryResponse findDistrictsContainGeometry(FindDistrictsContainGeometryRequest request) {
        String token = getToken();
        ResponseEntity<FindDistrictsContainGeometryResponse> response = supabaseClient.findDistrictsContainGeometry(request, token);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.error(
                    "[findDistrictsContainGeometry] Failed to find districts contain geometry {}",
                    request.getGeometry()
            );
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.CAN_NOT_FIND_DISTRICTS_CONTAIN_GEOMETRY);
        }

        return response.getBody();
    }
}
