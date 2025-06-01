package com.stride.tracking.metricservice.service.impl;

import com.stride.tracking.metric.dto.activity.event.ActivityCreatedEvent;
import com.stride.tracking.metric.dto.activity.event.ActivityDeletedEvent;
import com.stride.tracking.metric.dto.event.ActivityMetricEvent;
import com.stride.tracking.metric.dto.report.response.ActivityDetailReport;
import com.stride.tracking.metric.dto.report.response.ActivityReport;
import com.stride.tracking.metric.dto.report.response.SportDetailReport;
import com.stride.tracking.metric.dto.report.response.SportReport;
import com.stride.tracking.metricservice.mapper.ActivityMetricMapper;
import com.stride.tracking.metricservice.model.ActivityMetric;
import com.stride.tracking.metricservice.model.SportCache;
import com.stride.tracking.metricservice.repository.ActivityMetricRepository;
import com.stride.tracking.metricservice.service.ActivityMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ActivityMetricServiceImpl implements ActivityMetricService {
    private final ActivityMetricRepository activityMetricRepository;

    private final SportCacheService sportCacheService;

    private final ActivityMetricMapper activityMetricMapper;

    @Override
    @Transactional(readOnly = true)
    public ActivityReport getActivityReport(Instant from, Instant to) {
        List<ActivityMetric> activities = activityMetricRepository.findAllByTimeBetween(from, to);

        double totalDistance = 0.0;
        double totalTime = 0.0;
        double totalElevationGain = 0.0;
        Set<String> users = new HashSet<>();
        for (ActivityMetric activity : activities) {
            totalDistance += activity.getDistance();
            totalTime += activity.getMovingTimeSeconds();
            totalElevationGain += activity.getElevationGain();

            users.add(activity.getUserId());
        }

        List<ActivityDetailReport> activityDetailReports = activities
                .subList(0, Math.min(ActivityReport.MAX_RECENT_ACTIVITIES, activities.size()))
                .stream()
                .map(activityMetricMapper::mapToReportDetail)
                .toList();

        return ActivityReport.builder()
                .numberActivity(activities.size())
                .numberUsers(users.size())
                .totalDistance(totalDistance)
                .totalElevationGain(totalElevationGain)
                .totalTime(totalTime)
                .recentActivities(activityDetailReports)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SportReport getSportReport(Instant from, Instant to) {
        List<ActivityMetric> activities = activityMetricRepository.findAllByTimeBetween(from, to);

        Map<String, Integer> activityBySport = new HashMap<>();
        for (ActivityMetric activity : activities) {
            activityBySport.compute(activity.getSportId(), (k, v) -> v == null ? 1 : v + 1);
        }

        int numberHasMap = 0;
        int numberDoNotHaveMap = 0;
        List<SportDetailReport> sports = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : activityBySport.entrySet()) {
            Optional<SportCache> sportCache = sportCacheService.getOptionalSport(entry.getKey());
            if (sportCache.isEmpty()) {
                continue;
            }

            SportCache sport = sportCache.get();

            if (sport.getSportMapType() != null) {
                numberHasMap++;
            } else {
                numberDoNotHaveMap++;
            }

            sports.add(SportDetailReport.builder()
                    .id(sport.getId())
                    .name(sport.getName())
                    .color(sport.getColor())
                    .sportMapType(sport.getSportMapType())
                    .numberActivities(entry.getValue())
                    .build());
        }

        return SportReport.builder()
                .numberSports(activityBySport.size())
                .numberDoNotHaveMap(numberDoNotHaveMap)
                .numberHasMap(numberHasMap)
                .sports(sports)
                .build();
    }

    @Override
    @Transactional
    public void saveMetric(ActivityCreatedEvent event) {
        ActivityMetric activityMetric = activityMetricMapper.mapToModel(event);
        activityMetricRepository.save(activityMetric);
    }


    @Override
    @Transactional
    public void deleteMetric(ActivityDeletedEvent event) {
        activityMetricRepository.deleteByActivityId(event.getActivityId());
    }
}
