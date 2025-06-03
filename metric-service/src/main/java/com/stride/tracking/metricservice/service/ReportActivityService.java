package com.stride.tracking.metricservice.service;

import com.stride.tracking.metric.dto.report.response.activity.ActivityReport;
import com.stride.tracking.metricservice.model.ActivityMetric;

import java.util.List;

public interface ReportActivityService {
    ActivityReport getActivityReport(List<ActivityMetric> activityMetrics);
}
