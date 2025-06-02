package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.coreservice.client.ProfileFeignClient;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.profile.dto.profile.response.ProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProfileService {
    private final ProfileFeignClient profileClient;

    public ProfileResponse viewProfile() {
        ResponseEntity<ProfileResponse> response = profileClient.viewUser();
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.error("[viewProfile] Failed to view user profile for user id: {}", SecurityUtils.getCurrentUserId());
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.VIEW_PROFILE_FAILED);
        }

        log.debug("[viewProfile] Success to get user profile for user id: {}", response.getBody().getId());
        return Objects.requireNonNull(response.getBody());
    }
}
