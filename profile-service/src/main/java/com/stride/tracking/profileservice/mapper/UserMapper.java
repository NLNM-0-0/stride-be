package com.stride.tracking.profileservice.mapper;

import com.stride.tracking.profile.dto.user.response.UserResponse;
import com.stride.tracking.profileservice.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .ava(user.getAva())
                .city(user.getCity())
                .dob(user.getDob())
                .height(user.getHeight())
                .weight(user.getWeight())
                .male(user.getMale())
                .maxHeartRate(user.getMaxHeartRate())
                .heartRateZones(user.getHeartRateZones())
                .equipmentsWeight(user.getEquipmentWeight())
                .isBlock(user.isBlock())
                .build();
    }
}
