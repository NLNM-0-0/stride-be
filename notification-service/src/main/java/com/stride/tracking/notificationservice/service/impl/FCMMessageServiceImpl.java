package com.stride.tracking.notificationservice.service.impl;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.api.core.ApiFutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.stride.tracking.notificationservice.service.FCMMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class FCMMessageServiceImpl implements FCMMessageService {
    private final FirebaseMessaging firebaseMessaging;

    @Async
    @Override
    public void pushMessage(List<String> fcmTokens, String title, String body, String banner) {
        log.info("[pushMessage] Preparing FCM message: title='{}', body='{}', tokensCount={}", title, body, fcmTokens.size());

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(fcmTokens)
                .putData("title", title)
                .putData("body", body)
                .putData("banner", banner)
                .setNotification(
                        Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .setImage(banner)
                                .build()
                )
                .build();

        try {
            ApiFuture<BatchResponse> future = firebaseMessaging.sendEachForMulticastAsync(message);

            ApiFutures.addCallback(future, new ApiFutureCallback<>() {
                @Override
                public void onSuccess(BatchResponse result) {
                    log.info("[pushMessage] FCM push successful: {} success, {} failure", result.getSuccessCount(), result.getFailureCount());
                }

                @Override
                public void onFailure(Throwable t) {
                    log.error("[pushMessage] Failed to push FCM message to tokens: {}, error={}", fcmTokens, t.getMessage(), t);
                }
            }, MoreExecutors.directExecutor());

        } catch (Exception e) {
            log.error("[pushMessage] Exception during FCM push. Title: '{}', error={}", title, e.getMessage(), e);
        }
    }
}
