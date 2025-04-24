package com.stride.tracking.coreservice.utils;

import java.util.ArrayList;
import java.util.List;

public class RouteUtils {
    private RouteUtils() {}

    public static List<List<Double>> mergeCloseStartEnd(List<List<Double>> points, double thresholdMeters) {
        if (points.size() < 2) {
            return points;
        }

        List<Double> start = points.get(0);
        List<Double> end = points.get(points.size() - 1);

        double distance = GeometryUtils.distanceToPoint(start.get(1), start.get(0), end.get(1), end.get(0));

        if (distance <= thresholdMeters) {
            return new ArrayList<>(points.subList(0, points.size() - 1));
        }

        return points;
    }
}
