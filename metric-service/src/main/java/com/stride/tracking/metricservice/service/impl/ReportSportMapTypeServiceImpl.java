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
        // 0 for numberActivities
        // 1 for time
        Map<Instant, long[][]> grouped = new HashMap<>();

        for (ActivityMetric activity : activities) {
            Instant time = DateUtils.toStartOfDayInstant(activity.getTime(), zoneId);

            SportCache sportCache = sportCacheService.getSport(activity.getSportId());
            SportMapType type = sportCache.getSportMapType();

            long[][] counts = grouped.computeIfAbsent(time, d -> new long[SportMapType.values().length][2]);
            counts[type.ordinal()][0]++;
            counts[type.ordinal()][1] += activity.getMovingTimeSeconds();
        }

        List<SportMapTypeByDateEntryReport> result = new ArrayList<>();
        for (Map.Entry<Instant, long[][]> entry : grouped.entrySet()) {
            long[][] counts = entry.getValue();

            List<SportMapTypeByDateDetailReport> details = new ArrayList<>();
            for (int i = 0; i < SportMapType.values().length; i++) {
                details.add(
                        SportMapTypeByDateDetailReport.builder()
                                .type(SportMapType.values()[i])
                                .numberActivities((int) counts[i][0])
                                .time(counts[i][1])
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
