package com.stride.tracking.identityservice.service.impl;

import com.stride.tracking.dto.request.ResetPasswordUserRequest;
import com.stride.tracking.dto.request.ResetPasswordUserSendOTPRequest;
import com.stride.tracking.dto.request.VerifyResetPasswordRequest;
import com.stride.tracking.dto.response.VerifyResetPasswordResponse;

public interface ResetPasswordService {
    void sendOTPResetPassword(ResetPasswordUserSendOTPRequest request);
    VerifyResetPasswordResponse verifyResetPassword(VerifyResetPasswordRequest request);
    void resetPassword(String resetPasswordTokenId, ResetPasswordUserRequest request);
}
