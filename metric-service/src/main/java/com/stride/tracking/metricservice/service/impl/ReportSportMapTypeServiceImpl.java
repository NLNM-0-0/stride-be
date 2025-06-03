package com.stride.tracking.metricservice.service.impl;

import com.stride.tracking.commons.utils.DateUtils;
import com.stride.tracking.metric.dto.report.response.sportmaptype.SportMapTypeDetailReport;
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
    public List<SportMapTypeDetailReport> getSportMapTypesReport(
            ZoneId zoneId,
            List<ActivityMetric> activities
    ) {
        Map<Instant, EnumMap<SportMapType, Integer>> grouped = new HashMap<>();

        for (ActivityMetric activity : activities) {
            Instant time = DateUtils.toStartOfDayInstant(activity.getTime(), zoneId);

            SportCache sportCache = sportCacheService.getSport(activity.getSportId());
            SportMapType type = sportCache.getSportMapType();

            grouped
                    .computeIfAbsent(time, d -> new EnumMap<>(SportMapType.class))
                    .merge(type, 1, Integer::sum);
        }

        List<SportMapTypeDetailReport> result = new ArrayList<>();
        for (Map.Entry<Instant, EnumMap<SportMapType, Integer>> entry : grouped.entrySet()) {
            result.add(
                    SportMapTypeDetailReport.builder()
                            .from(DateUtils.toStartDate(entry.getKey(), zoneId))
                            .to(DateUtils.toEndDate(entry.getKey(), zoneId))
                            .values(entry.getValue())
                            .build()
            );
        }

        return result;
    }
}
