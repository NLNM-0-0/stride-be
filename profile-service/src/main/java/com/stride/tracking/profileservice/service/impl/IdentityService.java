package com.stride.tracking.profileservice.service.impl;

import com.stride.tracking.commons.utils.FeignClientHandler;
import com.stride.tracking.identity.dto.user.request.CreateUserIdentityRequest;
import com.stride.tracking.identity.dto.user.request.UpdateAdminUserIdentityRequest;
import com.stride.tracking.identity.dto.user.request.UpdateNormalUserIdentityRequest;
import com.stride.tracking.profileservice.client.IdentityFeignClient;
import com.stride.tracking.profileservice.constant.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class IdentityService {
    private final IdentityFeignClient identityFeignClient;

    public void updateUserIdentity(
            String id,
            UpdateAdminUserIdentityRequest request
    ) {
        FeignClientHandler.handleInternalCall(
                () -> identityFeignClient.updateAdminUserIdentity(id, request),
                HttpStatus.BAD_REQUEST,
                Message.UPDATE_USER_IDENTITY_FAILED
        );
    }

    public void updateUserIdentity(
            String id,
            UpdateNormalUserIdentityRequest request
    ) {
        FeignClientHandler.handleInternalCall(
                () -> identityFeignClient.updateNormalUserIdentity(id, request),
                HttpStatus.BAD_REQUEST,
                Message.UPDATE_USER_IDENTITY_FAILED
        );
    }

    public void createUserIdentity(
            CreateUserIdentityRequest request
    ) {
        FeignClientHandler.handleInternalCall(
                () -> identityFeignClient.createUserIdentity(request),
                HttpStatus.BAD_REQUEST,
                Message.CREATE_USER_IDENTITY_FAILED
        );
    }
}
