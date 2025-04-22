package com.stride.tracking.identityservice.service;

import com.stride.tracking.dto.register.request.VerifyResetPasswordRequest;
import com.stride.tracking.dto.resetpassword.request.ResetPasswordUserRequest;
import com.stride.tracking.dto.resetpassword.request.ResetPasswordUserSendOTPRequest;
import com.stride.tracking.dto.resetpassword.response.VerifyResetPasswordResponse;

public interface ResetPasswordService {
    void sendOTPResetPassword(ResetPasswordUserSendOTPRequest request);
    VerifyResetPasswordResponse verifyResetPassword(VerifyResetPasswordRequest request);
    void resetPassword(String resetPasswordTokenId, ResetPasswordUserRequest request);
}
