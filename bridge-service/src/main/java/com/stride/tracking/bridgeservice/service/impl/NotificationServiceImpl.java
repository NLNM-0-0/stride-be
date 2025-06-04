package com.stride.tracking.bridgeservice.service.impl;

import com.stride.tracking.bridge.dto.notification.request.NotificationFilter;
import com.stride.tracking.bridge.dto.notification.response.NotificationResponse;
import com.stride.tracking.bridgeservice.constant.Message;
import com.stride.tracking.bridgeservice.mapper.NotificationMapper;
import com.stride.tracking.bridgeservice.model.Notification;
import com.stride.tracking.bridgeservice.repository.NotificationRepository;
import com.stride.tracking.bridgeservice.repository.spec.NotificationCriteria;
import com.stride.tracking.bridgeservice.service.NotificationService;
import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.commons.dto.page.AppPageResponse;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    private final MongoTemplate mongoTemplate;

    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public ListResponse<NotificationResponse, NotificationFilter> getNotifications(
            AppPageRequest page,
            NotificationFilter filter
    ) {
        String userId = SecurityUtils.getCurrentUserId();

        Criteria criteria = filterNotifications(filter, userId);

        Query query = new Query(criteria)
                .with(Sort.by(Sort.Direction.DESC, "createdAt"))
                .with(PageRequest.of(page.getPage() - 1, page.getLimit()));

        long totalElements = mongoTemplate.count(new Query(criteria), Notification.class);
        int totalPages = (int) Math.ceil((double) totalElements / page.getLimit());

        List<Notification> notifications = mongoTemplate.find(query, Notification.class);

        List<NotificationResponse> data = notifications.stream()
                .map(notificationMapper::mapToNotificationResponse)
                .toList();

        return ListResponse.<NotificationResponse, NotificationFilter>builder()
                .data(data)
                .filter(filter)
                .appPageResponse(
                        AppPageResponse.builder()
                                .index(page.getPage())
                                .limit(page.getLimit())
                                .totalElements(totalElements)
                                .totalPages(totalPages)
                                .build()
                )
                .build();
    }

    private Criteria filterNotifications(NotificationFilter filter, String currentUserId) {
        NotificationCriteria criteria = new NotificationCriteria();
        criteria = criteria.haveUserId(currentUserId);
        if (filter.getSeen() != null) {
            criteria = criteria.haveSeen(filter.getSeen());
        }
        return criteria.build();
    }

    @Override
    @Transactional
    public void makeSeen(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(
                ()->new StrideException(HttpStatus.BAD_REQUEST, Message.NOTIFICATION_NOT_EXISTED)
        );

        String currentUserId = SecurityUtils.getCurrentUserId();
        if (!notification.getUserId().equals(currentUserId)) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.CAN_NOT_SEE_OTHER_USER_NOTIFICATIONS);
        }

        if (notification.isSeen()) {
            return;
        }

        notification.setSeen(true);

        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void makeSeenAll() {
        String currentUserId = SecurityUtils.getCurrentUserId();

        List<Notification> notifications = notificationRepository.findAllByUserId(currentUserId);

        for (Notification notification : notifications) {
            notification.setSeen(true);
        }

        notificationRepository.saveAll(notifications);
    }
}
