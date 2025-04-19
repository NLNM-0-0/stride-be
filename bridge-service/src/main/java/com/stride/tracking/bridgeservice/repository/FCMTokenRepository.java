package com.stride.tracking.bridgeservice.repository;

import com.stride.tracking.bridgeservice.model.FCMToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FCMTokenRepository extends MongoRepository<FCMToken, String> {
    List<FCMToken> findByUserId(String userId);
    Optional<FCMToken> findByToken(String token);
}
