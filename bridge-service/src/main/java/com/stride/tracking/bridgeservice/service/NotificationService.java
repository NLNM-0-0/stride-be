package com.stride.tracking.bridgeservice.service;

import com.stride.tracking.bridge.dto.notification.request.NotificationFilter;
import com.stride.tracking.bridge.dto.notification.response.NotificationResponse;
import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;

public interface NotificationService {
    ListResponse<NotificationResponse, NotificationFilter> getNotifications(
            AppPageRequest page,
            NotificationFilter filter
    );

    void makeSeen(String notificationId);

    void makeSeenAll();
}
