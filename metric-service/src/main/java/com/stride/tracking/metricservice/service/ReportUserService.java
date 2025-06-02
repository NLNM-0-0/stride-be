package com.stride.tracking.metricservice.service;

import com.stride.tracking.metric.dto.report.response.UserReport;

import java.time.Instant;

public interface ReportUserService {
    UserReport getUserReport(Instant from, Instant to);
}
