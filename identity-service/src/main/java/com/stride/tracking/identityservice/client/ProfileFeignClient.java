package com.stride.tracking.identityservice.client;

import com.stride.tracking.dto.request.CreateUserRequest;
import com.stride.tracking.dto.response.CreateUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "profile-service", url = "${app.services.profile}")
@Component
public interface ProfileFeignClient {
    @PostMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CreateUserResponse> createUser(CreateUserRequest request);
}
