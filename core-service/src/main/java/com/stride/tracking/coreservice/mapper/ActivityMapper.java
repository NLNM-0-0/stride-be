package com.stride.tracking.coreservice.mapper;

import com.stride.tracking.coreservice.model.Activity;
import com.stride.tracking.coreservice.model.HeartRateZoneValue;
import com.stride.tracking.dto.activity.response.ActivityResponse;
import com.stride.tracking.dto.activity.response.ActivityShortResponse;
import com.stride.tracking.dto.activity.response.ActivityUserResponse;
import com.stride.tracking.dto.activity.response.HeartRateZoneResponse;
import com.stride.tracking.dto.sport.response.SportResponse;
import com.stride.tracking.dto.user.response.UserResponse;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.stream.Collectors;

@Component
public class ActivityMapper {
    public ActivityShortResponse mapToShortResponse(Activity activity, SportResponse sport, ActivityUserResponse user) {
        return ActivityShortResponse.builder()
                .id(activity.getId())
                .name(activity.getName())
                .sport(sport)
                .user(user)
                .totalDistance(activity.getTotalDistance())
                .elevationGain(activity.getElevationGain())
                .movingTimeSeconds(activity.getMovingTimeSeconds())
                .mapImage(activity.getMapImage())
                .createdAt(Date.from(activity.getCreatedAt()))
                .build();
    }

    public ActivityResponse mapToActivityResponse(Activity activity, SportResponse sport, ActivityUserResponse user) {
        return ActivityResponse.builder()
                .id(activity.getId())
                .name(activity.getName())
                .description(activity.getDescription())
                .sport(sport)
                .user(user)
                .distances(activity.getDistances())
                .totalDistance(activity.getTotalDistance())
                .elapsedTimeSeconds(activity.getElapsedTimeSeconds())
                .movingTimeSeconds(activity.getMovingTimeSeconds())
                .calories(activity.getCalories())
                .carbonSaved(activity.getCarbonSaved())
                .rpe(activity.getRpe())
                .images(activity.getImages())
                .mapImage(activity.getMapImage())
                .geometry(activity.getGeometry())
                .elevations(activity.getElevations())
                .elevationGain(activity.getElevationGain())
                .maxElevation(activity.getMaxElevation())
                .speeds(activity.getSpeeds())
                .avgSpeed(activity.getAvgSpeed())
                .maxSpeed(activity.getMaxSpeed())
                .heartRates(activity.getHeartRates())
                .heartRateZones(activity.getHeartRateZones().stream().map(
                        this::mapToHeartRateZoneResponse
                ).collect(Collectors.toList()))
                .avgHearRate(activity.getAvgHearRate())
                .maxHearRate(activity.getMaxHearRate())
                .createdAt(Date.from(activity.getCreatedAt()))
                .build();
    }

    private HeartRateZoneResponse mapToHeartRateZoneResponse(
            HeartRateZoneValue heartRateZoneValue) {
        return HeartRateZoneResponse.builder()
                .zoneId(heartRateZoneValue.zone().getId())
                .name(heartRateZoneValue.zone().getName())
                .min(heartRateZoneValue.min())
                .max(heartRateZoneValue.max())
                .value(heartRateZoneValue.value())
                .build();
    }

    public ActivityUserResponse mapToUserResponse(UserResponse user) {
        return ActivityUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .ava(user.getAva())
                .build();
    }
}
