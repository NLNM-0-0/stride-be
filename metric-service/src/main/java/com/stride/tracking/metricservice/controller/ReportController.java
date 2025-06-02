package com.stride.tracking.metricservice.controller;

import com.stride.tracking.commons.annotations.PreAuthorizeAdmin;
import com.stride.tracking.commons.constants.CustomHeaders;
import com.stride.tracking.metric.dto.report.request.ReportFilter;
import com.stride.tracking.metric.dto.report.response.GetReportResponse;
import com.stride.tracking.metricservice.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping
    @PreAuthorizeAdmin
    ResponseEntity<GetReportResponse> getReport(
            @RequestHeader(value = CustomHeaders.X_USER_TIMEZONE, defaultValue = "UTC") String timezone,
            @Valid ReportFilter filter
    ) {
        ZoneId zoneId = ZoneId.of(timezone);

        return ResponseEntity.ok(reportService.getReport(zoneId, filter));
    }
}
