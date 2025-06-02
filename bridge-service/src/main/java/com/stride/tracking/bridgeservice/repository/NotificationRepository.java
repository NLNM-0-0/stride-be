package com.stride.tracking.bridgeservice.repository;

import com.stride.tracking.bridgeservice.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findAllByUserId(String userId);
}
