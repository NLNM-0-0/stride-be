package com.stride.tracking.coreservice.utils;

import com.stride.tracking.dto.mapbox.response.MapboxWayPoint;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComposeNameHelper {
    private ComposeNameHelper() {
    }

    public static String composeRouteName(List<MapboxWayPoint> waypoints) {
        if (waypoints == null || waypoints.isEmpty()) {
            return "";
        }

        return waypoints.stream()
                .limit(3)
                .map(MapboxWayPoint::getName)
                .collect(Collectors.joining(" - "));
    }

    public static String composeLocationName(String ward, String district, String city) {
        return Stream.of(ward, district, city)
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining(", "));
    }
}
