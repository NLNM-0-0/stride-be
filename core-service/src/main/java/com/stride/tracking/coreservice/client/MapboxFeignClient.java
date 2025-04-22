package com.stride.tracking.coreservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "map-box",
        url = "https://api.mapbox.com/"
)
@Component
public interface MapboxFeignClient {
    @GetMapping("/styles/v1/{mapStyle}/static/path-2+2571db({path})/auto/{width}x{height}")
    ResponseEntity<byte[]> getStaticMapImage(
            @PathVariable("mapStyle") String mapStyle,
            @PathVariable("path") String path,
            @PathVariable("width") int width,
            @PathVariable("height") int height,
            @RequestParam("padding") int padding,
            @RequestParam("access_token") String accessToken
    );
}
