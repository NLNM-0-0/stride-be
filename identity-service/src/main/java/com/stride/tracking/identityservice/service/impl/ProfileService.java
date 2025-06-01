package com.stride.tracking.identityservice.service.impl;

import com.stride.tracking.commons.configuration.kafka.KafkaProducer;
import com.stride.tracking.commons.constants.KafkaTopics;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.identityservice.client.ProfileFeignClient;
import com.stride.tracking.identityservice.constant.AuthProvider;
import com.stride.tracking.identityservice.constant.Message;
import com.stride.tracking.metric.dto.user.event.UserCreatedEvent;
import com.stride.tracking.profile.dto.user.request.CreateUserRequest;
import com.stride.tracking.profile.dto.user.response.CreateUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProfileService {
    private final ProfileFeignClient profileClient;

    private final KafkaProducer kafkaProducer;

    public String createUser(CreateUserRequest request, AuthProvider provider) {
        log.debug("[createUser] Creating profile for new user: {}", request.getName());

        ResponseEntity<CreateUserResponse> response = profileClient.createUser(request);
        if (response.getStatusCode() != HttpStatus.CREATED || response.getBody() == null) {
            log.error("[createUser] Failed to create user profile, response: {}", response);
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.PROFILE_CREATE_USER_ERROR);
        }

        String userId = Objects.requireNonNull(response.getBody()).getUserId();
        log.debug("[createUser] Created new profile with userId: {}", userId);

        sendUserCreatedMetric(userId, provider);

        return userId;
    }

    private void sendUserCreatedMetric(
            String userId,
            AuthProvider provider
    ) {
        kafkaProducer.send(
                KafkaTopics.USER_CREATED_TOPIC,
                UserCreatedEvent.builder()
                        .userId(userId)
                        .provider(provider.toString())
                        .time(Instant.now())
                        .build()
        );
    }
}
