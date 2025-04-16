package com.stride.tracking.notificationservice.service.impl;

import com.stride.tracking.dto.request.EmailRequest;
import com.stride.tracking.dto.request.SendEmailRequest;
import com.stride.tracking.notificationservice.model.Notification;
import com.stride.tracking.notificationservice.repository.NotificationRepository;
import com.stride.tracking.notificationservice.client.MailSender;
import com.stride.tracking.notificationservice.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class GmailService implements MailService {
    private final NotificationRepository notificationRepository;
    private final MailSender mailSender;

    @Transactional
    @Override
    public void sendNotification(SendEmailRequest request) {
        log.info("[sendNotification] Start sending notification to userId={}, subject={}",
                request.getTo().getId(), request.getSubject());

        saveNotification(request);

        sendEmail(request);

        log.info("[sendNotification] Notification sent successfully to userId={}", request.getTo().getId());
    }

    private void saveNotification(SendEmailRequest request) {
        log.debug("[saveNotification] Saving notification for userId={}, subject={}",
                request.getTo().getId(), request.getSubject());

        Notification notification = Notification.builder()
                .userId(request.getTo().getId())
                .title(request.getSubject())
                .body(request.getHtmlContent())
                .seen(false)
                .build();

        notificationRepository.save(notification);

        log.debug("[saveNotification] Notification saved for userId={}", request.getTo().getId());
    }

    private void sendEmail(SendEmailRequest request) {
        log.debug("[sendEmail] Preparing email request for userId={}, subject={}",
                request.getTo().getId(), request.getSubject());

        EmailRequest emailRequest = EmailRequest.builder()
                .to(List.of(request.getTo()))
                .subject(request.getSubject())
                .htmlContent(request.getHtmlContent())
                .build();

        mailSender.sendMail(emailRequest);

        log.debug("[sendEmail] Email sent to userId={}", request.getTo().getId());
    }
}
