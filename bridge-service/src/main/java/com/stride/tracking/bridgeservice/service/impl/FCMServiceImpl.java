package com.stride.tracking.bridgeservice.service.impl;

import com.stride.tracking.commons.exception.ResourceAlreadyExistException;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.dto.request.FCMTokenRequest;
import com.stride.tracking.dto.request.PushFCMNotificationRequest;
import com.stride.tracking.bridgeservice.constant.Message;
import com.stride.tracking.bridgeservice.model.FCMToken;
import com.stride.tracking.bridgeservice.model.Notification;
import com.stride.tracking.bridgeservice.repository.FCMTokenRepository;
import com.stride.tracking.bridgeservice.repository.NotificationRepository;
import com.stride.tracking.bridgeservice.service.FCMMessageService;
import com.stride.tracking.bridgeservice.service.FCMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class FCMServiceImpl implements FCMService {
    private final FCMTokenRepository fcmTokenRepository;
    private final NotificationRepository notificationRepository;

    private final FCMMessageService fcmMessageService;

    @Override
    @Transactional
    public void saveFCMToken(FCMTokenRequest request) {
        log.info("[saveFCMToken] Attempting to save FCM token: {}", request.getToken());

        fcmTokenRepository.findByToken(request.getToken()).ifPresent(fcmToken -> {
            log.error("[saveFCMToken] FCM token already exists: {}", request.getToken());
            throw new ResourceAlreadyExistException("FCMToken", "token", request.getToken());
        });

        String userId = SecurityUtils.getCurrentUserId();
        log.debug("[saveFCMToken] Saving FCM token for userId={}", userId);

        fcmTokenRepository.save(FCMToken.builder()
                .token(request.getToken())
                .userId(userId)
                .build());

        log.info("[saveFCMToken] Successfully saved FCM token for userId={}", userId);
    }

    @Override
    @Transactional
    public void deleteFCMTokenByToken(String token) {
        log.info("[deleteFCMTokenByToken] Attempting to delete FCM token: {}", token);

        FCMToken fcmToken = fcmTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.error("[deleteFCMTokenByToken] FCM token does not exist: {}", token);
                    return new StrideException(HttpStatus.BAD_REQUEST, Message.FCM_TOKEN_NOT_EXIST);
                });

        fcmTokenRepository.delete(fcmToken);

        log.info("[deleteFCMTokenByToken] Successfully deleted FCM token: {}", token);
    }

    @Override
    @Transactional
    public void deleteFCMTokenByUserId(String userId) {
        log.info("[deleteFCMTokenByUserId] Deleting all FCM tokens for userId={}", userId);

        List<FCMToken> fcmTokens = findByUserId(userId);

        fcmTokenRepository.deleteAll(fcmTokens);

        log.info("[deleteFCMTokenByUserId] Deleted {} FCM tokens for userId={}", fcmTokens.size(), userId);
    }

    public List<FCMToken> findByUserId(String userId) {
        return fcmTokenRepository.findByUserId(userId);
    }

    @Override
    public void pushNotification(PushFCMNotificationRequest request) {
        log.info("[pushNotification] Pushing notification to userId={}, title={}", request.getUserId(), request.getTitle());

        saveNotification(request);

        pushMessage(request);

        log.info("[pushNotification] Pushing notification to userId={}, title={}", request.getUserId(), request.getTitle());
    }

    private void saveNotification(PushFCMNotificationRequest request) {
        log.debug("[saveNotification] Saving notification for userId={}, title={}", request.getUserId(), request.getTitle());

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .body(request.getMessage())
                .seen(false)
                .build();

        notificationRepository.save(notification);

        log.debug("[saveNotification] Notification saved for userId={}", request.getUserId());
    }

    private void pushMessage(PushFCMNotificationRequest request) {
        List<FCMToken> fcmTokenEntities = findByUserId(request.getUserId());
        List<String> fcmTokens = fcmTokenEntities.stream().map(FCMToken::getToken).toList();

        log.debug("[pushMessage] Pushing message to {} FCM tokens for userId={}", fcmTokens.size(), request.getUserId());

        fcmMessageService.pushMessage(
                fcmTokens,
                request.getTitle(),
                request.getMessage(),
                request.getBanner()
        );

        log.debug("[pushMessage] Message pushed to FCM tokens for userId={}", request.getUserId());
    }
}
