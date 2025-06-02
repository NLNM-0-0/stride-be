package com.stride.tracking.profileservice.mapper;

import com.stride.tracking.profile.dto.profile.response.ProfileResponse;
import com.stride.tracking.profile.dto.user.response.UserResponse;
import com.stride.tracking.profileservice.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public ProfileResponse mapToUserResponse(User user) {
        return ProfileResponse.builder()
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
                .isBlock(user.isBlocked())
                .build();
    }

    public UserResponse mapToUserManagementResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .ava(user.getAva())
                .isAdmin(user.isAdmin())
                .isBlocked(user.isBlocked())
                .build();
    }
}
