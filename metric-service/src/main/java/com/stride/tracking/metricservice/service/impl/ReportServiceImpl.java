package com.stride.tracking.metricservice.service.impl;

import com.stride.tracking.commons.utils.DateUtils;
import com.stride.tracking.metric.dto.report.request.ReportFilter;
import com.stride.tracking.metric.dto.report.response.ActivityReport;
import com.stride.tracking.metric.dto.report.response.GetReportResponse;
import com.stride.tracking.metric.dto.report.response.SportReport;
import com.stride.tracking.metric.dto.report.response.UserReport;
import com.stride.tracking.metricservice.service.ReportActivityService;
import com.stride.tracking.metricservice.service.ReportService;
import com.stride.tracking.metricservice.service.ReportSportService;
import com.stride.tracking.metricservice.service.ReportUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final ReportActivityService reportActivityService;
    private final ReportSportService reportSportService;
    private final ReportUserService reportUserService;

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

        ActivityReport activityReport = reportActivityService.getActivityReport(from, to);
        UserReport userReport = reportUserService.getUserReport(from, to);
        SportReport sportReport = reportSportService.getSportReport(from, to);

        return GetReportResponse.builder()
                .activity(activityReport)
                .sportReport(sportReport)
                .userReport(userReport)
                .build();
    }
}
