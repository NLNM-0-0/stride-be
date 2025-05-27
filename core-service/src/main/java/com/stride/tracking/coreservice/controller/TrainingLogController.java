package com.stride.tracking.coreservice.controller;

import com.stride.tracking.commons.constants.CustomHeaders;
import com.stride.tracking.commons.dto.ListWithoutPagingResponse;
import com.stride.tracking.coreservice.service.TrainingLogService;
import com.stride.tracking.dto.traininglog.request.TrainingLogFilter;
import com.stride.tracking.dto.traininglog.response.TrainingLogResponse;
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
    ResponseEntity<ListWithoutPagingResponse<TrainingLogResponse, TrainingLogFilter>> getTrainingLogs(
            @RequestHeader(value = CustomHeaders.X_USER_TIMEZONE, defaultValue = "UTC") String timezone,
            @Valid TrainingLogFilter filter
    ) {
        ZoneId zoneId = ZoneId.of(timezone);

        return ResponseEntity.ok(trainingLogService.getTrainingLogs(filter, zoneId));
    }
}
