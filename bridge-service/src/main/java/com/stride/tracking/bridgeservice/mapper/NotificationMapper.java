package com.stride.tracking.bridgeservice.mapper;

import com.stride.tracking.bridge.dto.notification.response.NotificationResponse;
import com.stride.tracking.bridgeservice.model.Notification;
import com.stride.tracking.commons.utils.DateUtils;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationResponse mapToNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .createdAt(DateUtils.toDate(notification.getCreatedAt()))
                .seen(notification.isSeen())
                .build();
    }
}
