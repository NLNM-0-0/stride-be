package com.stride.tracking.metricservice.controller;

import com.stride.tracking.commons.annotations.PreAuthorizeUser;
import com.stride.tracking.commons.constants.CustomHeaders;
import com.stride.tracking.commons.dto.ListWithMetadataResponse;
import com.stride.tracking.metric.dto.traininglog.request.TrainingLogFilter;
import com.stride.tracking.metric.dto.traininglog.response.TrainingLogMetadata;
import com.stride.tracking.metric.dto.traininglog.response.TrainingLogResponse;
import com.stride.tracking.metricservice.service.TrainingLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;

@RestController
@RequestMapping("/training-logs")
@RequiredArgsConstructor
public class TrainingLogController {
    private final TrainingLogService trainingLogService;

    @GetMapping("/profile")
    @PreAuthorizeUser
    ResponseEntity<ListWithMetadataResponse<TrainingLogResponse, TrainingLogFilter, TrainingLogMetadata>> getTrainingLogs(
            @RequestHeader(value = CustomHeaders.X_USER_TIMEZONE, defaultValue = "UTC") String timezone,
            @Valid TrainingLogFilter filter
    ) {
        ZoneId zoneId = ZoneId.of(timezone);

        return ResponseEntity.ok(trainingLogService.getTrainingLogs(zoneId, filter));
    }
}
