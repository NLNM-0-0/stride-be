package com.stride.tracking.coreservice.mapper;

import com.stride.tracking.dto.route.response.LocationResponse;
import com.stride.tracking.dto.route.response.RouteResponse;
import com.stride.tracking.coreservice.model.Location;
import com.stride.tracking.coreservice.model.Route;
import com.stride.tracking.coreservice.utils.StridePolylineUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteMapper {
    public RouteResponse mapToRouteResponse(Route route) {
        List<String> allImages = route.getImages() == null
                ? List.of()
                : route.getImages().values()
                .stream()
                .flatMap(List::stream)
                .toList();

        return RouteResponse.builder()
                .id(route.getId())
                .sportId(route.getSport().getId())
                .userId(route.getUserId())
                .name(route.getName())
                .avgTime(route.getTotalTime() / route.getHeat())
                .avgDistance(route.getTotalDistance() / route.getHeat())
                .mapImage(route.getMapImage())
                .images(allImages)
                .districts(route.getDistricts())
                .geometry(StridePolylineUtils.encode(route.getGeometry()))
                .location(mapToLocationResponse(route.getLocation()))
                .heat(route.getHeat())
                .build();
    }

    private LocationResponse mapToLocationResponse(Location location) {
        return LocationResponse.builder()
                .ward(location.getWard())
                .district(location.getDistrict())
                .city(location.getCity())
                .build();
    }
}
