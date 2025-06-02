package com.stride.tracking.profileservice.client;

import com.stride.tracking.commons.configuration.feign.FeignConfig;
import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.identity.dto.user.request.CreateUserIdentityRequest;
import com.stride.tracking.identity.dto.user.request.UpdateAdminUserIdentityRequest;
import com.stride.tracking.identity.dto.user.request.UpdateNormalUserIdentityRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "identity-service",
        url = "${app.services.identity}",
        configuration = FeignConfig.class
)
@Component
public interface IdentityFeignClient {
    @PutMapping(path = "/manage/admin/by-user-id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<SimpleResponse> updateAdminUserIdentity(
            @PathVariable("id") String id,
            @RequestBody UpdateAdminUserIdentityRequest request
    );

    @PutMapping(path = "/manage/normal/by-user-id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<SimpleResponse> updateNormalUserIdentity(
            @PathVariable("id") String id,
            @RequestBody UpdateNormalUserIdentityRequest request
    );

    @PostMapping(path = "/manage", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<SimpleResponse> createUserIdentity(
            @RequestBody CreateUserIdentityRequest request
    );
}
