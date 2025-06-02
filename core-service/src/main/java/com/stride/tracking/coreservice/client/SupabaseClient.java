package com.stride.tracking.coreservice.client;

import com.stride.tracking.core.dto.supabase.request.FindDistrictsContainGeometryRequest;
import com.stride.tracking.core.dto.supabase.request.FindDistrictsNearPointRequest;
import com.stride.tracking.core.dto.supabase.request.FindNearestWayPointsRequest;
import com.stride.tracking.core.dto.supabase.request.GetLocationByGeometryRequest;
import com.stride.tracking.core.dto.supabase.response.FindDistrictsContainGeometryResponse;
import com.stride.tracking.core.dto.supabase.response.FindDistrictsNearPointResponse;
import com.stride.tracking.core.dto.supabase.response.FindNearestWayPointsResponse;
import com.stride.tracking.core.dto.supabase.response.GetLocationByGeometryResponse;
import com.stride.tracking.coreservice.configuration.SupabaseFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "supabase",
        url = "${app.services.supabase}",
        configuration = SupabaseFeignConfig.class
)
@Component
public interface SupabaseClient {
    @PostMapping(
            value = "/functions/v1/get_location_by_geometry",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<GetLocationByGeometryResponse> getLocationByGeometry(
            @RequestBody GetLocationByGeometryRequest request,
            @RequestHeader("Authorization") String authorization
    );

    @PostMapping(value = "/functions/v1/find_districts_contain_geometry", consumes = "application/json")
    ResponseEntity<FindDistrictsContainGeometryResponse> findDistrictsContainGeometry(
            @RequestBody FindDistrictsContainGeometryRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    );

    @PostMapping(value = "/functions/v1/find_districts_near_point", consumes = "application/json")
    ResponseEntity<FindDistrictsNearPointResponse> findDistrictsNearPoint(
            @RequestBody FindDistrictsNearPointRequest data,
            @RequestHeader("Authorization") String authorizationHeader
    );

    @PostMapping(value = "/functions/v1/find_nearest_way_points", consumes = "application/json")
    ResponseEntity<FindNearestWayPointsResponse> findNearestWayPoints(
            @RequestBody FindNearestWayPointsRequest data,
            @RequestHeader("Authorization") String authorizationHeader
    );
}
