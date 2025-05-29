package com.stride.tracking.bridgeservice.controller;

import com.stride.tracking.bridgeservice.service.FCMService;
import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.dto.fcm.request.FCMTokenRequest;
import com.stride.tracking.dto.fcm.request.PushFCMNotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fcm")
public class FCMController {
    private final FCMService fcmService;

    @PostMapping
    @PermitAll
    ResponseEntity<SimpleResponse> saveFCMToken(@RequestBody FCMTokenRequest request) {
        fcmService.saveFCMToken(request);
        return new ResponseEntity<>(new SimpleResponse(), HttpStatus.CREATED);
    }

    @DeleteMapping("/tokens/{token}")
    @PermitAll
    ResponseEntity<SimpleResponse> deleteFCMTokenById(@PathVariable String token) {
        fcmService.deleteFCMTokenByToken(token);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @DeleteMapping("/users/{userId}")
    @PermitAll
    ResponseEntity<SimpleResponse> deleteFCMTokenByUserId(@PathVariable String userId) {
        fcmService.deleteFCMTokenByUserId(userId);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @PostMapping("/message")
    @PermitAll
    ResponseEntity<SimpleResponse> pushMessage(@RequestBody PushFCMNotificationRequest request) {
        fcmService.pushNotification(request);
        return new ResponseEntity<>(new SimpleResponse(), HttpStatus.CREATED);
    }
}
