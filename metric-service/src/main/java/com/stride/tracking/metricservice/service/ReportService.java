package com.stride.tracking.metricservice.service;

import com.stride.tracking.metric.dto.report.request.ReportFilter;
import com.stride.tracking.metric.dto.report.response.GetReportResponse;

import java.time.ZoneId;

public interface ReportService {
    GetReportResponse getReport(ZoneId zoneId, ReportFilter reportFilter);
}
