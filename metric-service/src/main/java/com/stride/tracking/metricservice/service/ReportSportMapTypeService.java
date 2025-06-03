package com.stride.tracking.metricservice.service;

import com.stride.tracking.metric.dto.report.response.sportmaptype.SportMapTypeByDateEntryReport;
import com.stride.tracking.metricservice.model.ActivityMetric;

import java.time.ZoneId;
import java.util.List;

public interface ReportSportMapTypeService {
    List<SportMapTypeByDateEntryReport> getSportMapTypesReport(
            ZoneId zoneId,
            List<ActivityMetric> activities
    );
}
