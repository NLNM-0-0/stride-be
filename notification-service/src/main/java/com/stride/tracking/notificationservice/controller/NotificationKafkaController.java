package com.stride.tracking.notificationservice.controller;

import com.stride.tracking.dto.event.SendEmailEvent;
import com.stride.tracking.dto.request.Recipient;
import com.stride.tracking.dto.request.SendEmailRequest;
import com.stride.tracking.notificationservice.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationKafkaController {
    private final MailService mailService;

    @KafkaListener(topics = "notification-delivery")
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
