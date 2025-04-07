package com.stride.tracking.identityservice.service;

import com.stride.tracking.dto.request.EmailRegisterRequest;
import com.stride.tracking.dto.request.VerifyAccountRequest;
import com.stride.tracking.dto.response.EmailRegisterResponse;

public interface UserIdentityService {
    EmailRegisterResponse register(EmailRegisterRequest request);
    void verifyAccount(String userIdentityId, VerifyAccountRequest request);
    void sendVerifiedOTP(String userIdentityId);
}
