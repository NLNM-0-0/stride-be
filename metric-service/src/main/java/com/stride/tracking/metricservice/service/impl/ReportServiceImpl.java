package com.stride.tracking.metricservice.service.impl;

import com.stride.tracking.commons.utils.DateUtils;
import com.stride.tracking.metric.dto.report.request.ReportFilter;
import com.stride.tracking.metric.dto.report.response.GetReportResponse;
import com.stride.tracking.metric.dto.report.response.activity.ActivityReport;
import com.stride.tracking.metric.dto.report.response.sport.SportReport;
import com.stride.tracking.metric.dto.report.response.sportmaptype.SportMapTypeByDateEntryReport;
import com.stride.tracking.metric.dto.report.response.user.UserReport;
import com.stride.tracking.metricservice.model.ActivityMetric;
import com.stride.tracking.metricservice.repository.ActivityMetricRepository;
import com.stride.tracking.metricservice.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final ActivityMetricRepository activityMetricRepository;

    private final ReportActivityService reportActivityService;
    private final ReportSportService reportSportService;
    private final ReportUserService reportUserService;
    private final ReportSportMapTypeService reportSportMapTypeService;

    @Override
    public GetReportResponse getReport(ZoneId zoneId, ReportFilter reportFilter) {
        Instant from = DateUtils.toStartOfDayInstant(
                DateUtils.toInstant(reportFilter.getFromDate()),
                zoneId
        );
        Instant to = DateUtils.toEndOfDayInstant(
                DateUtils.toInstant(reportFilter.getToDate()),
                zoneId
        );

        List<ActivityMetric> activities = activityMetricRepository.findAllByTimeBetween(from, to);

        UserReport userReport = reportUserService.getUserReport(from, to);
        ActivityReport activityReport = reportActivityService.getActivityReport(activities);
        SportReport sportReport = reportSportService.getSportReport(activities);
        List<SportMapTypeByDateEntryReport> sportMapTypeDetailReports = reportSportMapTypeService.getSportMapTypesReport(
                zoneId,
                activities
        );

        return GetReportResponse.builder()
                .activity(activityReport)
                .sportReport(sportReport)
                .userReport(userReport)
                .sportMapTypes(sportMapTypeDetailReports)
                .build();
    }
}
