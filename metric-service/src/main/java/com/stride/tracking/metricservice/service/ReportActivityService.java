package com.stride.tracking.metricservice.service;

import com.stride.tracking.metric.dto.report.response.ActivityReport;

import java.time.Instant;

public interface ReportActivityService {
    ActivityReport getActivityReport(Instant from, Instant to);
}
