package com.stride.tracking.metricservice.service;

import com.stride.tracking.metric.dto.report.response.SportReport;

import java.time.Instant;

public interface ReportSportService {
    SportReport getSportReport (Instant from, Instant to);
}
