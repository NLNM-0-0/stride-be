package com.stride.tracking.metricservice.service;

import com.stride.tracking.metric.dto.report.response.sport.SportReport;
import com.stride.tracking.metricservice.model.ActivityMetric;

import java.util.List;

public interface ReportSportService {
    SportReport getSportReport (List<ActivityMetric> activities);
}
