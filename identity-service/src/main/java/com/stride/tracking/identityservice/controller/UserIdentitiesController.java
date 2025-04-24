package com.stride.tracking.identityservice.controller;

import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.dto.register.request.EmailRegisterRequest;
import com.stride.tracking.dto.register.request.VerifyAccountRequest;
import com.stride.tracking.dto.register.request.VerifyResetPasswordRequest;
import com.stride.tracking.dto.register.response.EmailRegisterResponse;
import com.stride.tracking.dto.resetpassword.request.ResetPasswordUserRequest;
import com.stride.tracking.dto.resetpassword.request.ResetPasswordUserSendOTPRequest;
import com.stride.tracking.dto.resetpassword.response.VerifyResetPasswordResponse;
import com.stride.tracking.identityservice.service.ResetPasswordService;
import com.stride.tracking.identityservice.service.UserIdentityService;
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
    private final ResetPasswordService resetPasswordService;

    @PostMapping("/register")
    ResponseEntity<EmailRegisterResponse> createUser(@RequestBody EmailRegisterRequest request) {
        EmailRegisterResponse response = identityService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/verify")
    ResponseEntity<SimpleResponse> verifyUser(
            @PathVariable String id,
            @RequestBody VerifyAccountRequest request) {
        identityService.verifyAccount(id, request);
        return new ResponseEntity<>(new SimpleResponse(), HttpStatus.OK);
    }

    @PostMapping("/{id}/verify/send-otp")
    ResponseEntity<SimpleResponse> sendOTPVerifyUser(
            @PathVariable String id) {
        identityService.sendVerifiedOTP(id);
        return new ResponseEntity<>(new SimpleResponse(), HttpStatus.OK);
    }

    @PostMapping("/reset-password/send-otp")
    ResponseEntity<SimpleResponse> sendOTPResetPassword(
            @RequestBody ResetPasswordUserSendOTPRequest request
    ) {
        resetPasswordService.sendOTPResetPassword(request);
        return new ResponseEntity<>(new SimpleResponse(), HttpStatus.OK);
    }

    @PostMapping("/reset-password/verify")
    ResponseEntity<VerifyResetPasswordResponse> verifyResetPasswordToken(
            @RequestBody VerifyResetPasswordRequest request
    ) {
        VerifyResetPasswordResponse response = resetPasswordService.verifyResetPassword(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/reset-password/{resetPasswordTokenId}/change-password")
    ResponseEntity<SimpleResponse> resetPassword(
            @PathVariable String resetPasswordTokenId,
            @RequestBody ResetPasswordUserRequest request
    ) {
        resetPasswordService.resetPassword(resetPasswordTokenId, request);
        return new ResponseEntity<>(new SimpleResponse(), HttpStatus.OK);
    }
}
