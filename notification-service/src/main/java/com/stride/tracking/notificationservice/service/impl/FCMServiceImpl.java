package com.stride.tracking.notificationservice.service.impl;

import com.stride.tracking.commons.exception.ResourceAlreadyExistException;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.dto.request.FCMTokenRequest;
import com.stride.tracking.dto.request.PushFCMNotificationRequest;
import com.stride.tracking.notificationservice.constant.Message;
import com.stride.tracking.notificationservice.model.FCMToken;
import com.stride.tracking.notificationservice.model.Notification;
import com.stride.tracking.notificationservice.repository.FCMTokenRepository;
import com.stride.tracking.notificationservice.repository.NotificationRepository;
import com.stride.tracking.notificationservice.service.FCMMessageService;
import com.stride.tracking.notificationservice.service.FCMService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FCMServiceImpl implements FCMService {
    private final FCMTokenRepository fcmTokenRepository;
    private final NotificationRepository notificationRepository;

    private final FCMMessageService fcmMessageService;

    @Override
    @Transactional
    public void saveFCMToken(FCMTokenRequest request) {
        fcmTokenRepository.findByToken(request.getToken()).ifPresent(fcmToken -> {
            throw new ResourceAlreadyExistException("FCMToken", "token", request.getToken());
        });

        String userId = SecurityUtils.getCurrentUserId();

        fcmTokenRepository.save(FCMToken.builder()
                .token(request.getToken())
                .userId(userId)
                .build());
    }

    @Override
    @Transactional
    public void deleteFCMTokenByToken(String token) {
        FCMToken fcmToken = fcmTokenRepository.
                findByToken(token).orElseThrow(() ->
                        new StrideException(HttpStatus.BAD_REQUEST, Message.FCM_TOKEN_NOT_EXIST)
                );

        fcmTokenRepository.delete(fcmToken);
    }

    @Override
    @Transactional
    public void deleteFCMTokenByUserId(String userId) {
        List<FCMToken> fcmTokens = findByUserId(userId);

        fcmTokenRepository.deleteAll(fcmTokens);
    }

    public List<FCMToken> findByUserId(String userId) {
        return fcmTokenRepository.findByUserId(userId);
    }

    @Override
    public void pushNotification(PushFCMNotificationRequest request) {
        saveNotification(request);

        pushMessage(request);
    }

    private void saveNotification(PushFCMNotificationRequest request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .body(request.getMessage())
                .seen(false)
                .build();

        notificationRepository.save(notification);
    }

    private void pushMessage(PushFCMNotificationRequest request){
        List<FCMToken> fcmTokenEntities = findByUserId(request.getUserId());
        List<String> fcmTokens = fcmTokenEntities.stream().map(FCMToken::getToken).toList();

        fcmMessageService.pushMessage(
                fcmTokens,
                request.getTitle(),
                request.getMessage(),
                request.getBanner()
        );
    }
}
