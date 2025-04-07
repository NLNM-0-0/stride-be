package com.stride.tracking.profileservice.service.impl;

import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.commons.utils.UpdateHelper;
import com.stride.tracking.dto.constant.HeartRateZone;
import com.stride.tracking.dto.request.CreateUserRequest;
import com.stride.tracking.dto.request.UpdateUserRequest;
import com.stride.tracking.dto.response.CreateUserResponse;
import com.stride.tracking.dto.response.UserResponse;
import com.stride.tracking.profileservice.constant.Message;
import com.stride.tracking.profileservice.model.User;
import com.stride.tracking.profileservice.repository.UserRepository;
import com.stride.tracking.profileservice.service.UserService;
import com.stride.tracking.profileservice.utils.DobHelper;
import com.stride.tracking.profileservice.utils.heartrate.MaxHearRateCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.stride.tracking.profileservice.constant.AppConstant.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final MaxHearRateCalculator maxHearRateCalculator;

    @Transactional
    @Override
    public CreateUserResponse createNewUser(CreateUserRequest request) {
        User user = User.builder()
                .name(request.getName())
                .ava(request.getAva() != null ? request.getAva() : DEFAULT_AVA)
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
                .male(user.getMale())
                .maxHeartRate(user.getMaxHeartRate())
                .heartRateZones(user.getHeartRateZones())
                .equipmentsWeight(user.getEquipmentsWeight())
                .isBlock(user.isBlock())
                .build();

    }

    @Transactional
    @Override
    public void updateUserProfile(UpdateUserRequest request) {
        validateHeartRate(request);

        String currUserId = SecurityUtils.getCurrentUserId();

        User user = userRepository.findById(currUserId).orElseThrow(
                () -> new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_EXIST)
        );

        UpdateHelper.updateIfNotNull(request.getName(), user::setName);
        UpdateHelper.updateIfNotNull(request.getAva(), user::setAva);
        UpdateHelper.updateIfNotNull(request.getCity(), user::setCity);
        UpdateHelper.updateIfNotNull(request.getHeight(), user::setHeight);
        UpdateHelper.updateIfNotNull(request.getWeight(), user::setWeight);
        UpdateHelper.updateIfNotNull(request.getMale(), user::setMale);
        UpdateHelper.updateIfNotNull(request.getEquipmentsWeight(), user::setEquipmentsWeight);

        if (request.getDob() != null) {
            // User haven't changed maxHeartRate yet
            if (user.getMaxHeartRate() == null) {
                int age = DobHelper.getAge(request.getDob());
                int maxHeartRate = maxHearRateCalculator.calculate(age);

                user.setMaxHeartRate(maxHeartRate);
                user.setHeartRateZones(calculateHeartRateZone(maxHeartRate));
            }
            user.setDob(request.getDob());
        }


        if (request.getMaxHeartRate() != null) {
            user.setMaxHeartRate(request.getMaxHeartRate());
            user.setHeartRateZones(calculateHeartRateZone(request.getMaxHeartRate()));
        }
        UpdateHelper.updateIfNotNull(request.getHeartRateZones(), user::setHeartRateZones);

        userRepository.save(user);
    }

    private void validateHeartRate(UpdateUserRequest request) {
        if (request.getHeartRateZones() != null &&
                request.getMaxHeartRate() != null) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.JUST_CAN_UPDATE_HEART_RATE_ZONE_ONE_WAY);
        }

        if (request.getHeartRateZones() != null &&
                request.getHeartRateZones().size() != HeartRateZone.values().length) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.MUST_HAVE_ENOUGH_FIVE_HEART_RATE_ZONE);
        }
    }

    private Map<HeartRateZone, Integer> calculateHeartRateZone(int maxHeartRate) {
        return Map.of(
                HeartRateZone.ZONE1, Math.round(maxHeartRate * HEART_RATE_ZONE_1_RATE),
                HeartRateZone.ZONE2, Math.round(maxHeartRate * HEART_RATE_ZONE_2_RATE),
                HeartRateZone.ZONE3, Math.round(maxHeartRate * HEART_RATE_ZONE_3_RATE),
                HeartRateZone.ZONE4, Math.round(maxHeartRate * HEART_RATE_ZONE_4_RATE),
                HeartRateZone.ZONE5, Math.round(maxHeartRate * HEART_RATE_ZONE_5_RATE)
        );
    }
}
