package com.stride.tracking.identityservice.controller;

import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.identity.dto.register.request.EmailRegisterRequest;
import com.stride.tracking.identity.dto.register.request.VerifyAccountRequest;
import com.stride.tracking.identity.dto.register.request.VerifyResetPasswordRequest;
import com.stride.tracking.identity.dto.register.response.EmailRegisterResponse;
import com.stride.tracking.identity.dto.password.request.ResetPasswordUserRequest;
import com.stride.tracking.identity.dto.password.request.ResetPasswordUserSendOTPRequest;
import com.stride.tracking.identity.dto.password.response.VerifyResetPasswordResponse;
import com.stride.tracking.identityservice.service.PasswordService;
import com.stride.tracking.identityservice.service.UserIdentityService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserIdentitiesController {
    private final UserIdentityService identityService;
    private final PasswordService passwordService;

    @PostMapping("/register")
    @PermitAll
    ResponseEntity<EmailRegisterResponse> createUser(
            @Valid @RequestBody EmailRegisterRequest request
    ) {
        EmailRegisterResponse response = identityService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/verify")
    @PermitAll
    ResponseEntity<SimpleResponse> verifyUser(
            @PathVariable String id,
            @RequestBody VerifyAccountRequest request) {
        identityService.verifyAccount(id, request);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @PostMapping("/{id}/verify/send-otp")
    @PermitAll
    ResponseEntity<SimpleResponse> sendOTPVerifyUser(
            @PathVariable String id) {
        identityService.sendVerifiedOTP(id);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @PostMapping("/reset-password/send-otp")
    @PermitAll
    ResponseEntity<SimpleResponse> sendOTPResetPassword(
            @RequestBody ResetPasswordUserSendOTPRequest request
    ) {
        passwordService.sendOTPResetPassword(request);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @PostMapping("/reset-password/verify")
    @PermitAll
    ResponseEntity<VerifyResetPasswordResponse> verifyResetPasswordToken(
            @RequestBody VerifyResetPasswordRequest request
    ) {
        VerifyResetPasswordResponse response = passwordService.verifyResetPassword(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/reset-password/{resetPasswordTokenId}/change-password")
    @PermitAll
    ResponseEntity<SimpleResponse> resetPassword(
            @PathVariable String resetPasswordTokenId,
            @RequestBody ResetPasswordUserRequest request
    ) {
        passwordService.resetPassword(resetPasswordTokenId, request);
        return ResponseEntity.ok(new SimpleResponse());
    }
}
