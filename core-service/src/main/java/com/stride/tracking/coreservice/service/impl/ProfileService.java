package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.utils.FeignClientHandler;
import com.stride.tracking.coreservice.client.ProfileFeignClient;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.profile.dto.profile.response.ProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProfileService {
    private final ProfileFeignClient profileClient;

    public ProfileResponse viewProfile() {
        return FeignClientHandler.handleInternalCall(
                profileClient::viewUser,
                HttpStatus.INTERNAL_SERVER_ERROR,
                Message.VIEW_PROFILE_FAILED
        );
    }
}
