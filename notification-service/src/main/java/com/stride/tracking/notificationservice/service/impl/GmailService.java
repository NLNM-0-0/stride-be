package com.stride.tracking.notificationservice.service.impl;

import com.stride.tracking.dto.request.EmailRequest;
import com.stride.tracking.dto.request.SendEmailRequest;
import com.stride.tracking.notificationservice.model.Notification;
import com.stride.tracking.notificationservice.repository.NotificationRepository;
import com.stride.tracking.notificationservice.client.MailSender;
import com.stride.tracking.notificationservice.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GmailService implements MailService {
    private final NotificationRepository notificationRepository;
    private final MailSender mailSender;

    @Transactional
    @Override
    public void sendNotification(SendEmailRequest request) {
        saveNotification(request);

        sendEmail(request);
    }

    private void saveNotification(SendEmailRequest request) {
        Notification notification = Notification.builder()
                .userId(request.getTo().getId())
                .title(request.getSubject())
                .body(request.getHtmlContent())
                .seen(false)
                .build();

        notificationRepository.save(notification);
    }

    private void sendEmail(SendEmailRequest request) {
        EmailRequest emailRequest = EmailRequest.builder()
                .to(List.of(request.getTo()))
                .subject(request.getSubject())
                .htmlContent(request.getHtmlContent())
                .build();

        mailSender.sendMail(emailRequest);
    }
}
