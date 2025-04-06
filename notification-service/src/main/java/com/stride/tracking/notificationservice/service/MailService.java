package com.stride.tracking.notificationservice.service;

import com.stride.tracking.dto.request.SendEmailRequest;

public interface MailService {
    void sendNotification(SendEmailRequest request);
}
