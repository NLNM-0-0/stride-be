package com.stride.tracking.identityservice.service;

import com.stride.tracking.identity.dto.password.request.ChangePasswordRequest;
import com.stride.tracking.identity.dto.register.request.VerifyResetPasswordRequest;
import com.stride.tracking.identity.dto.password.request.ResetPasswordUserRequest;
import com.stride.tracking.identity.dto.password.request.ResetPasswordUserSendOTPRequest;
import com.stride.tracking.identity.dto.password.response.VerifyResetPasswordResponse;

public interface PasswordService {
    void sendOTPResetPassword(
            ResetPasswordUserSendOTPRequest request
    );

    VerifyResetPasswordResponse verifyResetPassword(
            VerifyResetPasswordRequest request
    );

    void resetPassword(
            String resetPasswordTokenId,
            ResetPasswordUserRequest request
    );

    void changePassword(
            ChangePasswordRequest request
    );
}
