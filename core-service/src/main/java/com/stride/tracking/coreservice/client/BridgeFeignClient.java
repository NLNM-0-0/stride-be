package com.stride.tracking.coreservice.client;

import com.stride.tracking.bridge.dto.response.FileLinkResponse;
import com.stride.tracking.commons.configuration.feign.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "bridge-service",
        url = "${app.services.bridge}",
        configuration = FeignConfig.class
)
@Component
public interface BridgeFeignClient {
    @PostMapping(path = "/files/raw", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<FileLinkResponse> uploadRawFile(
            @RequestBody byte[] data,
            @RequestParam("fileName") String fileName,
            @RequestParam(value = "contentType", defaultValue = "image/png") String contentType);
}
