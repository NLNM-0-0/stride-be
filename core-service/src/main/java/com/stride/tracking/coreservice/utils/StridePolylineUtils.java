package com.stride.tracking.coreservice.utils;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;

import java.util.List;

public class StridePolylineUtils {
    private StridePolylineUtils() {}

    public static String encode(List<double[]> coordinates) {
        List<Point> points = coordinates.stream().map(coordinate ->
                Point.fromLngLat(coordinate[0], coordinate[1])
        ).toList();

        return PolylineUtils.encode(points, 5);
    }
}
