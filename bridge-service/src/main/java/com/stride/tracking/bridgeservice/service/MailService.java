package com.stride.tracking.bridgeservice.service;

import com.stride.tracking.bridge.dto.email.request.SendEmailRequest;

public interface MailService {
    void sendNotification(SendEmailRequest request);
}
