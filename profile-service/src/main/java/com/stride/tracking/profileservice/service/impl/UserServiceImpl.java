package com.stride.tracking.profileservice.service.impl;

import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.commons.utils.UpdateHelper;
import com.stride.tracking.dto.request.CreateUserRequest;
import com.stride.tracking.dto.request.UpdateUserRequest;
import com.stride.tracking.dto.response.CreateUserResponse;
import com.stride.tracking.dto.response.UserResponse;
import com.stride.tracking.profileservice.constant.Message;
import com.stride.tracking.profileservice.model.User;
import com.stride.tracking.profileservice.repository.UserRepository;
import com.stride.tracking.profileservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public CreateUserResponse createNewUser(CreateUserRequest request) {
        User user = User.builder()
                .name(request.getName())
                .isBlock(false)
                .build();

        user = userRepository.save(user);

        return CreateUserResponse.builder()
                .userId(user.getId())
                .build();
    }

    @Transactional
    @Override
    public UserResponse viewProfile() {
        String currUserId = SecurityUtils.getCurrentUserId();

        User user = userRepository.findById(currUserId).orElseThrow(
                () -> new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_EXIST)
        );

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .ava(user.getAva())
                .city(user.getCity())
                .dob(user.getDob())
                .height(user.getHeight())
                .weight(user.getWeight())
                .male(user.isMale())
                .maxHeartRate(user.getMaxHeartRate())
                .heartRateZones(user.getHeartRateZones())
                .equipmentsWeight(user.getEquipmentsWeight())
                .build();

    }

    @Transactional
    @Override
    public void updateUserProfile(UpdateUserRequest request) {
        String currUserId = SecurityUtils.getCurrentUserId();

        User user = userRepository.findById(currUserId).orElseThrow(
                () -> new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_EXIST)
        );

        UpdateHelper.updateIfNotNull(request.getName(), user::setName);
        UpdateHelper.updateIfNotNull(request.getAva(), user::setAva);
        UpdateHelper.updateIfNotNull(request.getCity(), user::setCity);
        UpdateHelper.updateIfNotNull(request.getDob(), user::setDob);
        UpdateHelper.updateIfNotNull(request.getHeight(), user::setHeight);
        UpdateHelper.updateIfNotNull(request.getWeight(), user::setWeight);
        UpdateHelper.updateIfNotNull(request.getMale(), user::setMale);
        UpdateHelper.updateIfNotNull(request.getMaxHeartRate(), user::setMaxHeartRate);
        UpdateHelper.updateIfNotNull(request.getHeartRateZones(), user::setHeartRateZones);
        UpdateHelper.updateIfNotNull(request.getEquipmentsWeight(), user::setEquipmentsWeight);
    }
}
