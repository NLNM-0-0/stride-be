package com.stride.tracking.metricservice.service;

import com.stride.tracking.metric.dto.report.response.sportmaptype.SportMapTypeDetailReport;
import com.stride.tracking.metricservice.model.ActivityMetric;

import java.time.ZoneId;
import java.util.List;

public interface ReportSportMapTypeService {
    List<SportMapTypeDetailReport> getSportMapTypesReport(
            ZoneId zoneId,
            List<ActivityMetric> activities
    );
}
