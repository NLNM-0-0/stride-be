package com.stride.tracking.coreservice.controller;

import com.stride.tracking.commons.constants.CustomHeaders;
import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.coreservice.dto.progress.request.GetProgressActivityRequest;
import com.stride.tracking.coreservice.dto.progress.request.ProgressFilter;
import com.stride.tracking.coreservice.dto.progress.response.GetProgressActivityResponse;
import com.stride.tracking.coreservice.dto.progress.response.ProgressResponse;
import com.stride.tracking.coreservice.dto.progress.response.ProgressShortResponse;
import com.stride.tracking.coreservice.service.ProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
public class ProgressController {
    private final ProgressService progressService;

    @GetMapping("/profile")
    ResponseEntity<SimpleListResponse<ProgressResponse>> getProgress(
            @RequestHeader(value = CustomHeaders.X_USER_TIMEZONE, defaultValue = "UTC") String timezone
    ) {
        ZoneId zoneId = ZoneId.of(timezone);

        return ResponseEntity.ok(progressService.getProgress(zoneId));
    }

    @GetMapping("/profile/detail")
    ResponseEntity<SimpleListResponse<ProgressShortResponse>> getProgress(
            @RequestHeader(value = CustomHeaders.X_USER_TIMEZONE, defaultValue = "UTC") String timezone,
            @Valid ProgressFilter filter
    ) {
        ZoneId zoneId = ZoneId.of(timezone);

        return ResponseEntity.ok(progressService.getProgress(zoneId, filter));
    }

    @GetMapping("/profile/detail/activities")
    ResponseEntity<GetProgressActivityResponse> getProgressActivity(
            @Valid GetProgressActivityRequest request
    ) {
        return ResponseEntity.ok(progressService.getProgressActivity(request));
    }
}
