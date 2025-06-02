package com.stride.tracking.bridgeservice.controller;

import com.stride.tracking.bridge.dto.notification.request.NotificationFilter;
import com.stride.tracking.bridge.dto.notification.response.NotificationResponse;
import com.stride.tracking.bridgeservice.service.NotificationService;
import com.stride.tracking.commons.annotations.PreAuthorizeUser;
import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/profile")
    @PreAuthorizeUser
    ResponseEntity<ListResponse<NotificationResponse, NotificationFilter>> getNotifications(
            @Valid AppPageRequest page,
            @Valid NotificationFilter filter
    ) {
        return ResponseEntity.ok(notificationService.getNotifications(page, filter));
    }

    @PostMapping("/profile/seen/{id}")
    @PreAuthorizeUser
    ResponseEntity<SimpleResponse> makeSeenNotification(
            @PathVariable String id
    ) {
        notificationService.makeSeen(id);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @PostMapping("/profile/seen")
    @PreAuthorizeUser
    ResponseEntity<SimpleResponse> makeSeenNotification() {
        notificationService.makeSeenAll();
        return ResponseEntity.ok(new SimpleResponse());
    }
}
