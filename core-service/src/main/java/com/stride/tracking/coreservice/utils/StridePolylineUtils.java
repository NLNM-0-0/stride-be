package com.stride.tracking.coreservice.utils;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;

import java.util.List;

public class StridePolylineUtils {
    private StridePolylineUtils() {}

    public static String encode(List<List<Double>> coordinates) {
        List<Point> points = coordinates.stream().map(coordinate ->
                Point.fromLngLat(coordinate.get(0), coordinate.get(1))
        ).toList();

        return PolylineUtils.encode(points, 5);
    }
}
