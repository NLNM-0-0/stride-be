package com.stride.tracking.metricservice.service.impl;

import com.stride.tracking.commons.utils.DateUtils;
import com.stride.tracking.metric.dto.report.response.sportmaptype.SportMapTypeByDateDetailReport;
import com.stride.tracking.metric.dto.report.response.sportmaptype.SportMapTypeByDateEntryReport;
import com.stride.tracking.metric.dto.sport.SportMapType;
import com.stride.tracking.metricservice.model.ActivityMetric;
import com.stride.tracking.metricservice.model.SportCache;
import com.stride.tracking.metricservice.service.ReportSportMapTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportSportMapTypeServiceImpl implements ReportSportMapTypeService {
    private final SportCacheService sportCacheService;

    @Override
    public List<SportMapTypeByDateEntryReport> getSportMapTypesReport(
            ZoneId zoneId,
            List<ActivityMetric> activities
    ) {
        Map<Instant, int[]> grouped = new HashMap<>();

        for (ActivityMetric activity : activities) {
            Instant time = DateUtils.toStartOfDayInstant(activity.getTime(), zoneId);

            SportCache sportCache = sportCacheService.getSport(activity.getSportId());
            SportMapType type = sportCache.getSportMapType();

            int[] counts = grouped.computeIfAbsent(time, d -> new int[SportMapType.values().length]);
            counts[type.ordinal()]++;
        }

        List<SportMapTypeByDateEntryReport> result = new ArrayList<>();
        for (Map.Entry<Instant, int[]> entry : grouped.entrySet()) {
            int[] counts = entry.getValue();

            List<SportMapTypeByDateDetailReport> details = new ArrayList<>();
            for (int i = 0; i < SportMapType.values().length; i++) {
                details.add(
                        SportMapTypeByDateDetailReport.builder()
                                .type(SportMapType.values()[i])
                                .value(counts[i])
                                .build()
                );
            }

            result.add(
                    SportMapTypeByDateEntryReport.builder()
                            .from(DateUtils.toStartDate(entry.getKey(), zoneId))
                            .to(DateUtils.toEndDate(entry.getKey(), zoneId))
                            .values(details)
                            .build()
            );
        }

        return result;
    }
}
