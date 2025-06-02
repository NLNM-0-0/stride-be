package com.stride.tracking.coreservice.utils;

import com.stride.tracking.core.dto.sport.SportMapType;
import com.stride.tracking.core.dto.supabase.response.WayPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WayPointHelper {
    private WayPointHelper() {
    }

    public static double getRdpEpsilon(SportMapType mapType) {
        if (mapType == SportMapType.WALKING) {
            return 0.00005;
        }
        return 0.001;
    }

    public static int getSimplificationStep(SportMapType mapType) {
        if (mapType == SportMapType.WALKING) {
            return 5;
        }
        return 10;
    }

    public static List<WayPoint> filterPoints(List<WayPoint> points) {
        List<WayPoint> filtered = removeNone(points);
        filtered = filterByLocalRepetition(filtered, 2, 3);
        filtered = keepRunBoundaries(filtered);
        return filtered;
    }

    private static List<WayPoint> removeNone(List<WayPoint> points) {
        List<WayPoint> result = new ArrayList<>();
        for (WayPoint point : points) {
            if (point != null) {
                result.add(point);
            }
        }
        return result;
    }

    private static List<WayPoint> filterByLocalRepetition(List<WayPoint> points, int windowSize, int minCount) {
        int n = points.size();
        if (n < windowSize * 2 + 1) {
            return new ArrayList<>(points);
        }

        List<WayPoint> result = new ArrayList<>();

        for (int i = 0; i < windowSize; i++) {
            result.add(points.get(i));
        }

        for (int i = windowSize; i < n - windowSize; i++) {
            List<WayPoint> window = points.subList(i - windowSize, i + windowSize + 1);
            String name = points.get(i).getName();
            long count = window.stream().filter(p -> Objects.equals(p.getName(), name)).count();
            if (count >= minCount) {
                result.add(points.get(i));
            }
        }

        for (int i = n - windowSize; i < n; i++) {
            result.add(points.get(i));
        }

        return result;
    }

    private static List<WayPoint> keepRunBoundaries(List<WayPoint> points) {
        List<WayPoint> result = new ArrayList<>();
        if (points == null || points.isEmpty()) {
            return result;
        }

        int i = 0;
        while (i < points.size()) {
            int start = i;
            String currentName = points.get(i).getName();
            while (i + 1 < points.size() && Objects.equals(points.get(i + 1).getName(), currentName)) {
                i++;
            }
            int end = i;
            result.add(points.get(start));
            if (end != start) {
                result.add(points.get(end));
            }
            i++;
        }

        return result;
    }
}
