package com.stride.tracking.coreservice.utils;

public class GeometryUtils {
    private GeometryUtils() {}

    private static final int EARTH_RADIUS = 6371000;

    //haversine
    public static double distanceToPoint(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    public static double mToKm(double meters) {
        return meters * 3.6;
    }
}
