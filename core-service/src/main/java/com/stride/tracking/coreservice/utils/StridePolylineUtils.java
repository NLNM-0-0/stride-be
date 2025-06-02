package com.stride.tracking.coreservice.utils;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class StridePolylineUtils {
    private StridePolylineUtils() {}

    private static final int PRECISION = 5;

    public static String encode(List<List<Double>> coordinates) {
        List<Point> points = coordinates.stream().map(coordinate ->
                Point.fromLngLat(coordinate.get(0), coordinate.get(1)) //lat lon
        ).toList();

        return PolylineUtils.encode(points, PRECISION);
    }

    public static String encode(Geometry geometry) {
        List<Point> points = Stream.of(geometry.getCoordinates()).map(coordinate ->
                Point.fromLngLat(coordinate.x, coordinate.y)
        ).toList();

        return PolylineUtils.encode(points, PRECISION);
    }

    public static List<List<Double>> decode(String polyline) {
        List<Point> decoded = PolylineUtils.decode(polyline, 5);
        List<List<Double>> result = new ArrayList<>();
        for (Point point : decoded) {
            result.add(List.of(point.longitude(), point.latitude()));
        }
        return result;
    }
}
