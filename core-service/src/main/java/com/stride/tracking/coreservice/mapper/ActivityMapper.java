package com.stride.tracking.coreservice.mapper;

import com.stride.tracking.commons.utils.DateUtils;
import com.stride.tracking.core.dto.activity.response.*;
import com.stride.tracking.coreservice.model.Activity;
import com.stride.tracking.coreservice.model.HeartRateZoneValue;
import com.stride.tracking.coreservice.model.Location;
import com.stride.tracking.profile.dto.profile.response.ProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityMapper {
    private final SportMapper sportMapper;

    public ActivityShortResponse mapToShortResponse(Activity activity, ProfileResponse user) {
        return ActivityShortResponse.builder()
                .id(activity.getId())
                .name(activity.getName())
                .sport(sportMapper.mapToResponse(activity.getSport()))
                .user(mapToUserResponse(user))
                .totalDistance(activity.getTotalDistance())
                .elevationGain(activity.getElevationGain())
                .movingTimeSeconds(activity.getMovingTimeSeconds())
                .mapImage(activity.getMapImage())
                .location(activity.getLocation() != null ? mapToLocationResponse(activity.getLocation()) : null)
                .createdAt(DateUtils.toDate(activity.getCreatedAt()))
                .build();
    }

    private LocationResponse mapToLocationResponse(Location location) {
        return LocationResponse.builder()
                .ward(location.getWard())
                .district(location.getDistrict())
                .city(location.getCity())
                .build();
    }

    public ActivityResponse mapToActivityResponse(Activity activity, ProfileResponse user) {
        return ActivityResponse.builder()
                .id(activity.getId())
                .name(activity.getName())
                .description(activity.getDescription())
                .sport(sportMapper.mapToResponse(activity.getSport()))
                .user(mapToUserResponse(user))
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
                .location(activity.getLocation() != null ? mapToLocationResponse(activity.getLocation()) : null)
                .routeId(activity.getRouteId())
                .createdAt(DateUtils.toDate(activity.getCreatedAt()))
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

    private ActivityUserResponse mapToUserResponse(ProfileResponse user) {
        return ActivityUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .ava(user.getAva())
                .build();
    }
}
