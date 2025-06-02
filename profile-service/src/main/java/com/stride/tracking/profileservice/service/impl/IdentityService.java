package com.stride.tracking.profileservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stride.tracking.commons.dto.ErrorResponse;
import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.identity.dto.user.request.CreateUserIdentityRequest;
import com.stride.tracking.identity.dto.user.request.UpdateAdminUserIdentityRequest;
import com.stride.tracking.identity.dto.user.request.UpdateNormalUserIdentityRequest;
import com.stride.tracking.profileservice.client.IdentityFeignClient;
import com.stride.tracking.profileservice.constant.Message;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        try {
            ResponseEntity<SimpleResponse> response = identityFeignClient.updateAdminUserIdentity(id, request);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new StrideException(HttpStatus.BAD_REQUEST, Message.UPDATE_USER_IDENTITY_FAILED);
            }
        } catch (FeignException ex) {
            try {
                String body = ex.contentUTF8();
                ObjectMapper mapper = new ObjectMapper();
                ErrorResponse errorResponse = mapper.readValue(body, ErrorResponse.class);

                String errorMessage = errorResponse.getMessage();
                throw new StrideException(HttpStatus.BAD_REQUEST, errorMessage);
            } catch (Exception parsingEx) {
                throw new StrideException(HttpStatus.BAD_REQUEST, Message.UPDATE_USER_IDENTITY_FAILED);
            }
        }
    }

    public void updateUserIdentity(
            String id,
            UpdateNormalUserIdentityRequest request
    ) {
        try {
            ResponseEntity<SimpleResponse> response = identityFeignClient.updateNormalUserIdentity(id, request);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new StrideException(HttpStatus.BAD_REQUEST, Message.UPDATE_USER_IDENTITY_FAILED);
            }
        } catch (FeignException ex) {
            try {
                String body = ex.contentUTF8();
                ObjectMapper mapper = new ObjectMapper();
                ErrorResponse errorResponse = mapper.readValue(body, ErrorResponse.class);

                String errorMessage = errorResponse.getMessage();
                throw new StrideException(HttpStatus.BAD_REQUEST, errorMessage);
            } catch (Exception parsingEx) {
                throw new StrideException(HttpStatus.BAD_REQUEST, Message.UPDATE_USER_IDENTITY_FAILED);
            }
        }
    }

    public void createUserIdentity(
            CreateUserIdentityRequest request
    ) {
        try {
            ResponseEntity<SimpleResponse> response = identityFeignClient.createUserIdentity(request);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new StrideException(HttpStatus.BAD_REQUEST, Message.CREATE_USER_IDENTITY_FAILED);
            }
        } catch (FeignException ex) {
            try {
                String body = ex.contentUTF8();
                ObjectMapper mapper = new ObjectMapper();
                ErrorResponse errorResponse = mapper.readValue(body, ErrorResponse.class);

                String errorMessage = errorResponse.getMessage();
                throw new StrideException(HttpStatus.BAD_REQUEST, errorMessage);
            } catch (Exception parsingEx) {
                throw new StrideException(HttpStatus.BAD_REQUEST, Message.CREATE_USER_IDENTITY_FAILED);
            }
        }
    }
}
