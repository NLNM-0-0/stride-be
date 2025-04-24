package com.stride.tracking.identityservice.client;

import com.stride.tracking.commons.configuration.feign.FeignConfig;
import com.stride.tracking.dto.register.response.CreateUserResponse;
import com.stride.tracking.dto.user.request.CreateUserRequest;
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
    ResponseEntity<CreateUserResponse> createUser(
            CreateUserRequest request);
}
