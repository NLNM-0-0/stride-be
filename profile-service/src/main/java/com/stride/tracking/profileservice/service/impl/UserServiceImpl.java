package com.stride.tracking.profileservice.service.impl;

import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.commons.utils.UpdateHelper;
import com.stride.tracking.profile.dto.profile.HeartRateZone;
import com.stride.tracking.profile.dto.profile.request.CreateProfileRequest;
import com.stride.tracking.profile.dto.profile.request.UpdateProfileRequest;
import com.stride.tracking.profile.dto.profile.response.CreateProfileResponse;
import com.stride.tracking.profile.dto.profile.response.ProfileResponse;
import com.stride.tracking.profileservice.constant.Message;
import com.stride.tracking.profileservice.mapper.UserMapper;
import com.stride.tracking.profileservice.model.User;
import com.stride.tracking.profileservice.repository.UserRepository;
import com.stride.tracking.profileservice.service.UserService;
import com.stride.tracking.profileservice.utils.DobHelper;
import com.stride.tracking.profileservice.utils.heartrate.MaxHearRateCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.stride.tracking.profileservice.constant.AppConstant.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final MaxHearRateCalculator maxHearRateCalculator;

    private final UserMapper userMapper;

    @Override
    @Transactional
    public CreateProfileResponse createNewUser(CreateProfileRequest request) {
        log.info("[createNewUser] Creating new user: {}", request.getName());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .ava(request.getAva() != null ? request.getAva() : DEFAULT_AVA)
                .isBlocked(false)
                .build();

        user = userRepository.save(user);

        log.info("[createNewUser] Successfully saved user: {}", user.getId());

        return CreateProfileResponse.builder()
                .userId(user.getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse viewProfile() {
        String currUserId = SecurityUtils.getCurrentUserId();
        log.info("[viewProfile] Fetching profile for user ID: {}", currUserId);

        User user = userRepository.findById(currUserId).orElseThrow(
                () -> {
                    log.error("[viewProfile] User with ID {} not found", currUserId);
                    return new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_EXIST);
                }
        );

        log.info("[viewProfile] Successfully fetched profile for user ID: {}", user.getId());

        return userMapper.mapToUserResponse(user);
    }

    @Override
    @Transactional
    public void updateUserProfile(UpdateProfileRequest request) {
        String currUserId = SecurityUtils.getCurrentUserId();
        log.info("[updateUserProfile] Updating profile for user ID: {}", currUserId);

        validateHeartRate(request);

        User user = userRepository.findById(currUserId).orElseThrow(
                () -> {
                    log.error("[updateUserProfile] User with ID {} not found", currUserId);
                    return new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_EXIST);
                }
        );

        log.debug("[updateUserProfile] Updating user details for ID: {}", currUserId);
        UpdateHelper.updateIfNotNull(request.getName(), user::setName);
        UpdateHelper.updateIfNotNull(request.getAva(), user::setAva);
        UpdateHelper.updateIfNotNull(request.getCity(), user::setCity);
        UpdateHelper.updateIfNotNull(request.getHeight(), user::setHeight);
        UpdateHelper.updateIfNotNull(request.getWeight(), user::setWeight);
        UpdateHelper.updateIfNotNull(request.getMale(), user::setMale);
        UpdateHelper.updateIfNotNull(request.getEquipmentsWeight(), user::setEquipmentWeight);

        if (request.getDob() != null) {
            log.debug("[updateUserProfile] Updating date of birth for user ID: {}", currUserId);
            if (user.getMaxHeartRate() == null) {
                int age = DobHelper.getAge(request.getDob());
                int maxHeartRate = maxHearRateCalculator.calculate(age);

                user.setMaxHeartRate(maxHeartRate);
                user.setHeartRateZones(calculateHeartRateZone(maxHeartRate));
            }
            user.setDob(request.getDob());
        }


        if (request.getMaxHeartRate() != null) {
            log.debug("[updateUserProfile] Updating max heart rate for user ID: {}", currUserId);
            user.setMaxHeartRate(request.getMaxHeartRate());
            user.setHeartRateZones(calculateHeartRateZone(request.getMaxHeartRate()));
        }
        UpdateHelper.updateIfNotNull(request.getHeartRateZones(), user::setHeartRateZones);

        userRepository.save(user);
    }

    private void validateHeartRate(UpdateProfileRequest request) {
        if (request.getHeartRateZones() != null &&
                request.getMaxHeartRate() != null) {
            log.error("[validateHeartRate] Attempt to update both heart rate zones and max heart rate for user");
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.JUST_CAN_UPDATE_HEART_RATE_ZONE_ONE_WAY);
        }

        if (request.getHeartRateZones() != null &&
                request.getHeartRateZones().size() != HeartRateZone.values().length) {
            log.error("[validateHeartRate] Invalid number of heart rate zones for user: {}", request.getHeartRateZones().size());
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.MUST_HAVE_ENOUGH_FIVE_HEART_RATE_ZONE);
        }
    }

    private Map<HeartRateZone, Integer> calculateHeartRateZone(int maxHeartRate) {
        log.debug("[calculateHeartRateZone] Calculating heart rate zones for max heart rate: {}", maxHeartRate);
        return Map.of(
                HeartRateZone.ZONE1, Math.round(maxHeartRate * HEART_RATE_ZONE_1_RATE),
                HeartRateZone.ZONE2, Math.round(maxHeartRate * HEART_RATE_ZONE_2_RATE),
                HeartRateZone.ZONE3, Math.round(maxHeartRate * HEART_RATE_ZONE_3_RATE),
                HeartRateZone.ZONE4, Math.round(maxHeartRate * HEART_RATE_ZONE_4_RATE),
                HeartRateZone.ZONE5, Math.round(maxHeartRate * HEART_RATE_ZONE_5_RATE)
        );
    }
}
