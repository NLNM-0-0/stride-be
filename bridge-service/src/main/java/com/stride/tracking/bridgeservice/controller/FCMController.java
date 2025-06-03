package com.stride.tracking.bridgeservice.controller;

import com.stride.tracking.bridge.dto.fcm.request.FCMTokenRequest;
import com.stride.tracking.bridge.dto.fcm.request.PushFCMNotificationRequest;
import com.stride.tracking.bridgeservice.service.FCMService;
import com.stride.tracking.commons.annotations.PreAuthorizeAdmin;
import com.stride.tracking.commons.annotations.PreAuthorizeUser;
import com.stride.tracking.commons.dto.SimpleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fcm")
public class FCMController {
    private final FCMService fcmService;

    @PostMapping
    @PreAuthorizeUser
    ResponseEntity<SimpleResponse> saveFCMToken(
            @Valid @RequestBody FCMTokenRequest request
    ) {
        fcmService.saveFCMToken(request);
        return new ResponseEntity<>(new SimpleResponse(), HttpStatus.CREATED);
    }

    @DeleteMapping("/tokens/{token}")
    @PreAuthorizeUser
    ResponseEntity<SimpleResponse> deleteFCMTokenById(
            @PathVariable String token
    ) {
        fcmService.deleteFCMTokenByToken(token);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorizeUser
    ResponseEntity<SimpleResponse> deleteFCMTokenByUserId(
            @PathVariable String userId
    ) {
        fcmService.deleteFCMTokenByUserId(userId);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @PostMapping("/message")
    @PreAuthorizeAdmin
    ResponseEntity<SimpleResponse> pushMessage(
            @Valid @RequestBody PushFCMNotificationRequest request
    ) {
        fcmService.pushNotification(request);

        return new ResponseEntity<>(new SimpleResponse(), HttpStatus.CREATED);
    }
}
