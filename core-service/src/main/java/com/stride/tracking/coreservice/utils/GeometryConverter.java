package com.stride.tracking.coreservice.utils;

import org.locationtech.jts.geom.*;

import java.util.List;

public class GeometryConverter {
    private GeometryConverter() {
    }

    public static Geometry fromListDouble(List<List<Double>> coordinates, GeometryFactory geometryFactory) {
        Coordinate[] coords = coordinates.stream()
                .map(p -> new Coordinate(p.get(0), p.get(1))) //lon, lat
                .toArray(Coordinate[]::new);
        return geometryFactory.createLineString(coords);
    }
}
