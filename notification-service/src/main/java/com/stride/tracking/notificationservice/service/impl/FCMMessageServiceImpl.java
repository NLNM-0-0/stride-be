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
                    log.info("FCM sent successfully to {} devices", result.getSuccessCount());
                }

                @Override
                public void onFailure(Throwable t) {
                    log.error("Failed to send FCM message", t);
                }
            }, MoreExecutors.directExecutor());

        } catch (Exception e) {
            log.error("Exception while sending FCM message", e);
        }
    }
}
