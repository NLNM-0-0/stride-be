package com.stride.tracking.identityservice.controller;

import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.identity.dto.password.request.ChangePasswordRequest;
import com.stride.tracking.identityservice.service.PasswordService;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    private final PasswordService passwordService;

    @PostMapping("/change-password")
    @PermitAll
    ResponseEntity<SimpleResponse> changePassword(
            @RequestBody ChangePasswordRequest request
    ) {
        passwordService.changePassword(request);
        return ResponseEntity.ok(new SimpleResponse());
    }
}
