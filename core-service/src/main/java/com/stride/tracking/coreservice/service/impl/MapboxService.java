package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.coreservice.client.BridgeFeignClient;
import com.stride.tracking.coreservice.client.MapboxFeignClient;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.dto.response.FileLinkResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapboxService {
    private final MapboxFeignClient mapboxClient;
    private final BridgeFeignClient bridgeClient;

    @Value("${mapbox.style}")
    private String defaultStyle;

    @Value("${mapbox.stroke-width}")
    private int defaultStrokeWidth;

    @Value("${mapbox.stroke-color}")
    private String defaultStrokeColor;

    @Value("${mapbox.stroke-fill}")
    private String defaultStrokeFill;

    @Value("${mapbox.width}")
    private int defaultWidth;

    @Value("${mapbox.height}")
    private int defaultHeight;

    @Value("${mapbox.padding}")
    private int defaultPadding;

    @Value("${mapbox.access-token}")
    private String accessToken;

    @Value("${mapbox.content-type}")
    private String contentType;

    public String generateAndUpload(String path, String fileName) {
        byte[] imageData = generateImage(path);

        return uploadFile(imageData, fileName);
    }

    private byte[] generateImage(String path) {
        return generateImage(
                path,
                defaultStyle,
                defaultStrokeWidth,
                defaultStrokeColor,
                defaultStrokeFill,
                defaultWidth,
                defaultHeight,
                defaultPadding
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
        ResponseEntity<byte[]> response = mapboxClient.getStaticMapImage(
                style, strokeWidth, strokeColor, strokeFill, path, width, height, padding, accessToken
        );
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.error("[getMapImage] Failed to get map image for path: {}", path);
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.GENERATE_IMAGE_FAILED);
        }
        return response.getBody();
    }

    private String uploadFile(byte[] data, String name) {
        ResponseEntity<FileLinkResponse> response = bridgeClient.uploadRawFile(data, name, contentType);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.error("[uploadFile] Failed to upload path image");
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.UPLOAD_IMAGE_FAILED);
        }
        return response.getBody().getFile();
    }
}
