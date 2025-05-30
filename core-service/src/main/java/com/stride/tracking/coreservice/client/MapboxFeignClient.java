package com.stride.tracking.coreservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        name = "mapbox",
        url = "${app.services.mapbox}"
)
@Component
public interface MapboxFeignClient {
    @GetMapping("/directions/v5/mapbox/{mapType}/{coordinates}")
    Map<String, Object> getDirections(
            @PathVariable("mapType") String mapType,
            @PathVariable("coordinates") String coordinates,
            @RequestParam(value = "access_token") String accessToken,
            @RequestParam(value = "alternatives", defaultValue = "false") String alternatives,
            @RequestParam(value = "geometries", defaultValue = "geojson") String geometries,
            @RequestParam(value = "overview", defaultValue = "full") String overview,
            @RequestParam(value = "steps", defaultValue = "false") String steps,
            @RequestParam(value = "continue_straight", defaultValue = "true") String continueStraight
    );

    @GetMapping("/styles/v1/{mapStyle}/static/path-{strokeWidth}{strokeColor}{strokeFill}({path})/auto/{width}x{height}")
    ResponseEntity<byte[]> getStaticMapImage(
            @PathVariable("mapStyle") String mapStyle,
            @PathVariable("strokeWidth") int strokeWidth,
            @PathVariable("strokeColor") String strokeColor,
            @PathVariable("strokeFill") String strokeFill,
            @PathVariable("path") String path,
            @PathVariable("width") int width,
            @PathVariable("height") int height,
            @RequestParam("padding") int padding,
            @RequestParam("access_token") String accessToken
    );
}
