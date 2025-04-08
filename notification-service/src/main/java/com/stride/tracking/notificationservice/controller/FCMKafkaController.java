package com.stride.tracking.notificationservice.controller;

import com.stride.tracking.commons.constants.KafkaTopics;
import com.stride.tracking.dto.request.PushFCMNotificationRequest;
import com.stride.tracking.notificationservice.service.FCMService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FCMKafkaController {
    private final FCMService fcmService;

    @KafkaListener(topics = KafkaTopics.FCM_TOPIC)
    public void listenNotificationDelivery(PushFCMNotificationRequest message) {
        fcmService.pushNotification(message);
    }
}
