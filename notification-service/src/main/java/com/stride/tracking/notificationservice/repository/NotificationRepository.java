package com.stride.tracking.notificationservice.repository;

import com.stride.tracking.notificationservice.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notification, String> {
}
