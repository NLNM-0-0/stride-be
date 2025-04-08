package com.stride.tracking.notificationservice.service;

import java.util.List;

public interface FCMMessageService {
    void pushMessage(List<String> fcmTokens, String title, String body, String banner);
}
