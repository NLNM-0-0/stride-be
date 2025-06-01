package com.stride.tracking.identityservice.service;

import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.identity.dto.register.request.EmailRegisterRequest;
import com.stride.tracking.identity.dto.register.request.VerifyAccountRequest;
import com.stride.tracking.identity.dto.register.response.EmailRegisterResponse;

public interface UserIdentityService {
    EmailRegisterResponse register(EmailRegisterRequest request);
    SimpleResponse createUser(EmailRegisterRequest request);
    void verifyAccount(String userIdentityId, VerifyAccountRequest request);
    void sendVerifiedOTP(String userIdentityId);
}
