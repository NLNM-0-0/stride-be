package com.stride.tracking.identityservice.service.impl;

import com.stride.tracking.commons.configuration.kafka.KafkaProducer;
import com.stride.tracking.commons.constants.KafkaTopics;
import com.stride.tracking.commons.utils.FeignClientHandler;
import com.stride.tracking.identityservice.client.ProfileFeignClient;
import com.stride.tracking.identityservice.constant.AuthProvider;
import com.stride.tracking.identityservice.constant.Message;
import com.stride.tracking.metric.dto.user.event.UserCreatedEvent;
import com.stride.tracking.profile.dto.profile.request.CreateProfileRequest;
import com.stride.tracking.profile.dto.profile.response.CreateProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProfileService {
    private final ProfileFeignClient profileClient;

    private final KafkaProducer kafkaProducer;

    public String createProfile(CreateProfileRequest request, AuthProvider provider) {
        CreateProfileResponse response = FeignClientHandler.handleInternalCall(
                ()->profileClient.createProfile(request),
                HttpStatus.INTERNAL_SERVER_ERROR,
                Message.PROFILE_CREATE_USER_ERROR
        );

        String userId = response.getUserId();

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
