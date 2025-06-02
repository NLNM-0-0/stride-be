package com.stride.tracking.identityservice.client;

import com.stride.tracking.commons.configuration.feign.FeignConfig;
import com.stride.tracking.profile.dto.profile.request.CreateProfileRequest;
import com.stride.tracking.profile.dto.profile.response.CreateProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "profile-service",
        url = "${app.services.profile}",
        configuration = FeignConfig.class
)
@Component
public interface ProfileFeignClient {
    @PostMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CreateProfileResponse> createProfile(
            CreateProfileRequest request
    );
}
