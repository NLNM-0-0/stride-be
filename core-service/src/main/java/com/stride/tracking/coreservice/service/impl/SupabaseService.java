package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.coreservice.client.SupabaseClient;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.dto.supabase.request.GetLocationByGeometryRequest;
import com.stride.tracking.dto.supabase.response.GetLocationByGeometryResponse;
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

    public GetLocationByGeometryResponse getLocationByGeometry(GetLocationByGeometryRequest request) {
        String token = "Bearer " + accessToken;
        ResponseEntity<GetLocationByGeometryResponse> response = supabaseClient.getLocationByGeometry(request, token);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.error("[getLocationByGeometry] Failed to get location for geometry: {}", request.getGeometry());
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.CAN_NOT_GET_LOCATION_FOR_GEOMETRY);
        }

        return response.getBody();
    }
}
