package com.stride.tracking.bridgeservice.controller;

import com.stride.tracking.bridge.dto.email.event.SendEmailEvent;
import com.stride.tracking.bridge.dto.email.request.Recipient;
import com.stride.tracking.bridge.dto.email.request.SendEmailRequest;
import com.stride.tracking.bridgeservice.service.MailService;
import com.stride.tracking.commons.constants.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.security.PermitAll;

@Component
@RequiredArgsConstructor
public class NotificationKafkaController {
    private final MailService mailService;

    @KafkaListener(topics = KafkaTopics.NOTIFICATION_TOPIC)
    @PermitAll
    public void listenNotificationDelivery(SendEmailEvent message) {
        mailService.sendNotification(SendEmailRequest.builder()
                .to(Recipient.builder()
                        .id(message.getRecipient().getId())
                        .email(message.getRecipient().getEmail())
                        .name(message.getRecipient().getName())
                        .build())
                .subject(message.getSubject())
                .htmlContent(message.getBody())
                .build());
    }
}
