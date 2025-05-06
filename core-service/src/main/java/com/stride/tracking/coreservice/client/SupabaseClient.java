package com.stride.tracking.coreservice.client;

import com.stride.tracking.coreservice.configuration.SupabaseFeignConfig;
import com.stride.tracking.dto.supabase.request.GetLocationByGeometryRequest;
import com.stride.tracking.dto.supabase.response.GetLocationByGeometryResponse;
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
}
