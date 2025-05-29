package com.stride.tracking.bridgeservice.controller;

import com.stride.tracking.bridgeservice.service.FCMService;
import com.stride.tracking.commons.constants.KafkaTopics;
import com.stride.tracking.dto.fcm.request.PushFCMNotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.security.PermitAll;

@Component
@RequiredArgsConstructor
public class FCMKafkaController {
    private final FCMService fcmService;

    @KafkaListener(topics = KafkaTopics.FCM_TOPIC)
    @PermitAll
    public void listenNotificationDelivery(PushFCMNotificationRequest message) {
        fcmService.pushNotification(message);
    }
}
