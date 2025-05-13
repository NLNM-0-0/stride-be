package com.stride.tracking.coreservice.mapper;

import com.stride.tracking.coreservice.model.Activity;
import com.stride.tracking.coreservice.model.HeartRateZoneValue;
import com.stride.tracking.coreservice.model.Location;
import com.stride.tracking.dto.activity.response.*;
import com.stride.tracking.dto.sport.response.SportResponse;
import com.stride.tracking.dto.user.response.UserResponse;
import org.springframework.stereotype.Component;

import java.util.Date;

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
                .location(mapToLocationResponse(activity.getLocation()))
                .createdAt(Date.from(activity.getCreatedAt()))
                .build();
    }

    private LocationResponse mapToLocationResponse(Location location) {
        return LocationResponse.builder()
                .ward(location.getWard())
                .district(location.getDistrict())
                .city(location.getCity())
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
                ).toList())
                .avgHearRate(activity.getAvgHearRate())
                .maxHearRate(activity.getMaxHearRate())
                .location(mapToLocationResponse(activity.getLocation()))
                .routeId(activity.getRouteId())
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
