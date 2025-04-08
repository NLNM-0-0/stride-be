package com.stride.tracking.notificationservice.controller;

import com.stride.tracking.commons.response.SimpleResponse;
import com.stride.tracking.dto.request.FCMTokenRequest;
import com.stride.tracking.dto.request.PushFCMNotificationRequest;
import com.stride.tracking.notificationservice.service.FCMService;
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
    ResponseEntity<SimpleResponse> saveFCMToken(@RequestBody FCMTokenRequest request) {
        fcmService.saveFCMToken(request);
        return new ResponseEntity<>(new SimpleResponse(), HttpStatus.CREATED);
    }

    @DeleteMapping("/tokens/{token}")
    ResponseEntity<SimpleResponse> deleteFCMTokenById(@PathVariable String token) {
        fcmService.deleteFCMTokenByToken(token);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @DeleteMapping("/users/{userId}")
    ResponseEntity<SimpleResponse> deleteFCMTokenByUserId(@PathVariable String userId) {
        fcmService.deleteFCMTokenByUserId(userId);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @PostMapping("/message")
    ResponseEntity<SimpleResponse> pushMessage(@RequestBody PushFCMNotificationRequest request) {
        fcmService.pushNotification(request);
        return new ResponseEntity<>(new SimpleResponse(), HttpStatus.CREATED);
    }
}
