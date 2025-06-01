package com.stride.tracking.bridgeservice.service;

import com.stride.tracking.bridge.dto.fcm.request.FCMTokenRequest;
import com.stride.tracking.bridge.dto.fcm.request.PushFCMNotificationRequest;

public interface FCMService {
    void saveFCMToken(FCMTokenRequest request);
    void deleteFCMTokenByToken(String token);
    void deleteFCMTokenByUserId(String userId);
    void pushNotification(PushFCMNotificationRequest request);
}
