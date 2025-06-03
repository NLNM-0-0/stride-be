package com.stride.tracking.metricservice.service.impl;

import com.stride.tracking.commons.utils.UpdateHelper;
import com.stride.tracking.metric.dto.activity.event.ActivityCreatedEvent;
import com.stride.tracking.metric.dto.activity.event.ActivityDeletedEvent;
import com.stride.tracking.metric.dto.activity.event.ActivityUpdatedEvent;
import com.stride.tracking.metric.dto.report.response.activity.ActivityDetailReport;
import com.stride.tracking.metric.dto.report.response.activity.ActivityReport;
import com.stride.tracking.metric.dto.report.response.sport.SportDetailReport;
import com.stride.tracking.metric.dto.report.response.sport.SportMapTypeDetailReport;
import com.stride.tracking.metric.dto.report.response.sport.SportReport;
import com.stride.tracking.metric.dto.sport.SportMapType;
import com.stride.tracking.metricservice.mapper.ActivityMetricMapper;
import com.stride.tracking.metricservice.model.ActivityMetric;
import com.stride.tracking.metricservice.model.SportCache;
import com.stride.tracking.metricservice.repository.ActivityMetricRepository;
import com.stride.tracking.metricservice.service.ActivityMetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class ActivityMetricServiceImpl implements ActivityMetricService {
    private final ActivityMetricRepository activityMetricRepository;

    private final SportCacheService sportCacheService;

    private final ActivityMetricMapper activityMetricMapper;

    @Override
    @Transactional(readOnly = true)
    public ActivityReport getActivityReport(List<ActivityMetric> activities) {
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
    public SportReport getSportReport(List<ActivityMetric> activities) {
        Map<String, Integer> activityBySport = new HashMap<>();
        for (ActivityMetric activity : activities) {
            activityBySport.compute(activity.getSportId(), (k, v) -> v == null ? 1 : v + 1);
        }

        int[] sportMapTypesCounts = new int[SportMapType.values().length];
        List<SportDetailReport> sports = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : activityBySport.entrySet()) {
            Optional<SportCache> sportCache = sportCacheService.getOptionalSport(entry.getKey());
            if (sportCache.isEmpty()) {
                continue;
            }

            SportCache sport = sportCache.get();
            sportMapTypesCounts[sport.getSportMapType().ordinal()]++;

            sports.add(SportDetailReport.builder()
                    .id(sport.getId())
                    .name(sport.getName())
                    .color(sport.getColor())
                    .image(sport.getImage())
                    .sportMapType(sport.getSportMapType())
                    .numberActivities(entry.getValue())
                    .build());
        }

        return SportReport.builder()
                .numberSports(activityBySport.size())
                .sportMapTypes(
                        Arrays.stream(SportMapType.values())
                                .map(type-> SportMapTypeDetailReport
                                        .builder()
                                        .type(type)
                                        .numberActivities(sportMapTypesCounts[type.ordinal()])
                                        .build()
                                )
                                .toList())
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
    public void updateMetric(ActivityUpdatedEvent event) {
        Optional<ActivityMetric> activityMetricOptional = activityMetricRepository.findByActivityId(event.getActivityId());
        if (activityMetricOptional.isEmpty()) {
            log.error("Failed to update activity metric with id: {}", event.getActivityId());
            return;
        }

        ActivityMetric activity = activityMetricOptional.get();

        UpdateHelper.updateIfNotNull(event.getName(), activity::setName);

        activityMetricRepository.save(activity);
    }


    @Override
    @Transactional
    public void deleteMetric(ActivityDeletedEvent event) {
        activityMetricRepository.deleteByActivityId(event.getActivityId());
    }
}
