package com.stride.tracking.coreservice.client;

import com.stride.tracking.commons.configuration.feign.FeignConfig;
import com.stride.tracking.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "profile-service",
        url = "${app.services.profile}",
        configuration = FeignConfig.class
)
@Component
public interface ProfileFeignClient {
    @GetMapping(value = "/users/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserResponse> viewUser();
}
