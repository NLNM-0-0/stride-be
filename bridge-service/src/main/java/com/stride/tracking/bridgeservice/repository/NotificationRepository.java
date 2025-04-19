package com.stride.tracking.bridgeservice.repository;

import com.stride.tracking.bridgeservice.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notification, String> {
}
