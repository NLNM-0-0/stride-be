package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.bridge.dto.supabase.response.FileLinkResponse;
import com.stride.tracking.commons.utils.FeignClientHandler;
import com.stride.tracking.core.dto.mapbox.response.MapboxDirectionResponse;
import com.stride.tracking.core.dto.mapbox.response.MapboxWayPoint;
import com.stride.tracking.core.dto.sport.SportMapType;
import com.stride.tracking.coreservice.client.BridgeFeignClient;
import com.stride.tracking.coreservice.client.MapboxFeignClient;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.coreservice.utils.JsonHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapboxService {
    private final MapboxFeignClient mapboxClient;
    private final BridgeFeignClient bridgeClient;

    @Value("${mapbox.access-token}")
    private String accessToken;

    @Value("${mapbox.static-image.style}")
    private String staticImageDefaultStyle;

    @Value("${mapbox.static-image.stroke-width}")
    private int staticImageDefaultStrokeWidth;

    @Value("${mapbox.static-image.stroke-color}")
    private String staticImageDefaultStrokeColor;

    @Value("${mapbox.static-image.stroke-fill}")
    private String staticImageDefaultStrokeFill;

    @Value("${mapbox.static-image.width}")
    private int staticImageDefaultWidth;

    @Value("${mapbox.static-image.height}")
    private int staticImageDefaultHeight;

    @Value("${mapbox.static-image.padding}")
    private int staticImageDefaultPadding;

    @Value("${mapbox.static-image.content-type}")
    private String staticImageContentType;

    @Value("${mapbox.directions.alternatives}")
    private String directionsAlternatives;

    @Value("${mapbox.directions.geometries}")
    private String directionsGeometries;

    @Value("${mapbox.directions.overview}")
    private String directionsOverview;

    @Value("${mapbox.directions.steps}")
    private String directionsSteps;

    @Value("${mapbox.directions.continue_straight}")
    private String directionsContinueStraight;

    @Value("${mapbox.matching.geometries}")
    private String matchingGeometries;

    @Value("${mapbox.matching.overview}")
    private String matchingOverview;

    public MapboxDirectionResponse getBatchRoute(List<List<Double>> coordinates, SportMapType mapType) {
        int maxCoordsPerRequest = 25;
        List<List<List<Double>>> chunks = new ArrayList<>();

        for (int i = 0; i < coordinates.size(); i += maxCoordsPerRequest) {
            int end = Math.min(i + maxCoordsPerRequest, coordinates.size());
            chunks.add(coordinates.subList(i, end));
        }

        List<List<Double>> allCoordinates = new ArrayList<>();
        List<MapboxWayPoint> allWaypoints = new ArrayList<>();

        for (List<List<Double>> chunk : chunks) {
            MapboxDirectionResponse response = getRoute(chunk, mapType);
            allCoordinates.addAll(response.getCoordinates());
            allWaypoints.addAll(response.getWayPoints());
        }

        Map<String, MapboxWayPoint> waypointMap = new HashMap<>();
        for (MapboxWayPoint wp : allWaypoints) {
            String key = wp.getName().toLowerCase();

            waypointMap.compute(key, (k, v) -> {
                if (v == null) {
                    return MapboxWayPoint.builder()
                            .latitude(wp.getLatitude())
                            .longitude(wp.getLongitude())
                            .name(wp.getName())
                            .freq(wp.getFreq())
                            .build();
                }
                v.setFreq(v.getFreq() + wp.getFreq());
                return v;
            });
        }

        List<MapboxWayPoint> mergedWaypoints = new ArrayList<>(waypointMap.values());
        return new MapboxDirectionResponse(allCoordinates, mergedWaypoints);
    }

    public MapboxDirectionResponse getRoute(List<List<Double>> coordinates, SportMapType mapType) {
        String encodedCoords = encodeCoords(coordinates);

        Map<String, Object> response = FeignClientHandler.handleExternalCall(
                () -> mapboxClient.getDirections(
                        mapType.name().toLowerCase(), encodedCoords, accessToken,
                        directionsAlternatives, directionsGeometries, directionsOverview,
                        directionsSteps, directionsContinueStraight
                ),
                HttpStatus.INTERNAL_SERVER_ERROR,
                Message.CAN_NOT_GET_DIRECTIONS_FROM_MAPBOX
        );

        List<List<Double>> coords = (List<List<Double>>) JsonHelper.getNestedValue(response, "routes", "0", "geometry", "coordinates");
        List<Map<String, Object>> rawWps = (List<Map<String, Object>>) response.getOrDefault("waypoints", new ArrayList<>());

        Map<String, MapboxWayPoint> counter = new HashMap<>();
        for (Map<String, Object> wp : rawWps) {
            String name = (String) wp.get("name");
            if (name == null || name.isEmpty()) {
                continue;
            }

            if (counter.containsKey(name)) {
                MapboxWayPoint mapboxWayPoint = counter.get(name);
                mapboxWayPoint.setFreq(mapboxWayPoint.getFreq() + 1);
            } else {
                counter.put(
                        name,
                        MapboxWayPoint.builder()
                                .name(name)
                                .latitude((Double) JsonHelper.getNestedValue(wp, "location", "1"))
                                .longitude((Double) JsonHelper.getNestedValue(wp, "location", "0"))
                                .freq(1)
                                .build()
                );
            }
        }

        List<MapboxWayPoint> waypoints = new ArrayList<>(counter.values());

        waypoints.sort(Comparator.comparingInt(MapboxWayPoint::getFreq).reversed());

        return new MapboxDirectionResponse(coords, waypoints);
    }

    private String encodeCoords(List<List<Double>> coordinates) {
        return coordinates.stream()
                .map(coordinate -> String.format("%s,%s", coordinate.get(0), coordinate.get(1)))
                .collect(Collectors.joining(";"));
    }

    public String generateAndUpload(String path, String fileName) {
        byte[] imageData = generateImage(path);

        return uploadFile(imageData, fileName);
    }

    private byte[] generateImage(String path) {
        return generateImage(
                path,
                staticImageDefaultStyle,
                staticImageDefaultStrokeWidth,
                staticImageDefaultStrokeColor,
                staticImageDefaultStrokeFill,
                staticImageDefaultWidth,
                staticImageDefaultHeight,
                staticImageDefaultPadding
        );
    }

    private byte[] generateImage(
            String path,
            String style,
            int strokeWidth,
            String strokeColor,
            String strokeFill,
            int width,
            int height,
            int padding
    ) {
        return FeignClientHandler.handleExternalCall(
                () -> mapboxClient.getStaticMapImage(
                        style, strokeWidth, strokeColor, strokeFill, path, width, height, padding, accessToken
                ),
                HttpStatus.INTERNAL_SERVER_ERROR,
                Message.GENERATE_IMAGE_FAILED
        );
    }

    private String uploadFile(byte[] data, String name) {
        FileLinkResponse response = FeignClientHandler.handleInternalCall(
                () -> bridgeClient.uploadRawFile(data, name, staticImageContentType),
                HttpStatus.INTERNAL_SERVER_ERROR,
                Message.UPLOAD_IMAGE_FAILED
        );
        return response.getFile();
    }

    public List<List<Double>> getBatchMapMatchingPoints(SportMapType mapType, List<List<Double>> coordinates) {
        int maxCoordsPerRequest = 100;
        List<List<List<Double>>> chunks = new ArrayList<>();

        for (int i = 0; i < coordinates.size(); i += maxCoordsPerRequest) {
            int end = Math.min(i + maxCoordsPerRequest, coordinates.size());
            chunks.add(coordinates.subList(i, end));
        }

        List<List<Double>> allCoordinates = new ArrayList<>();

        for (List<List<Double>> chunk : chunks) {
            List<List<Double>> response = getMapMatchingPoints(mapType, chunk);
            allCoordinates.addAll(response);
        }

        return allCoordinates;
    }

    public List<List<Double>> getMapMatchingPoints(SportMapType mapType, List<List<Double>> coordinates) {
        String encodedCoords = encodeCoords(coordinates);

        Map<String, Object> response = FeignClientHandler.handleExternalCall(
                () -> mapboxClient.getMatching(
                        mapType.name().toLowerCase(),
                        encodedCoords,
                        accessToken,
                        matchingGeometries,
                        matchingOverview
                ),
                HttpStatus.INTERNAL_SERVER_ERROR,
                Message.CAN_NOT_GET_DIRECTIONS_FROM_MAPBOX
        );

        List<Object> tracePointsRaw = (List<Object>) response.getOrDefault("tracepoints", new ArrayList<>());
        List<List<Double>> result = new ArrayList<>();

        for (Object tpObj : tracePointsRaw) {
            if (tpObj instanceof Map) {
                Map<String, Object> tracePoint = (Map<String, Object>) tpObj;
                List<Double> location = (List<Double>) tracePoint.get("location");
                result.add(location);
            }
        }

        return result;
    }
}
