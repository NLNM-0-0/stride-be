package com.stride.tracking.bridgeservice.service;

import com.stride.tracking.dto.email.request.SendEmailRequest;

public interface MailService {
    void sendNotification(SendEmailRequest request);
}
