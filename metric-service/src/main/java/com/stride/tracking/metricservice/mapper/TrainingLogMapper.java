package com.stride.tracking.metricservice.mapper;

import com.stride.tracking.commons.utils.DateUtils;
import com.stride.tracking.metric.dto.traininglog.response.TrainingLogActivityResponse;
import com.stride.tracking.metric.dto.traininglog.response.TrainingLogResponse;
import com.stride.tracking.metricservice.constant.SportColorConst;
import com.stride.tracking.metricservice.model.ActivityMetric;
import com.stride.tracking.metricservice.model.SportCache;
import com.stride.tracking.metricservice.service.impl.SportCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TrainingLogMapper {
    private final SportCacheService sportCacheService;

    private final SportCacheMapper sportMapper;

    public TrainingLogResponse mapToTrainingLogResponse(
            List<ActivityMetric> progresses,
            Date date
    ) {
        List<TrainingLogActivityResponse> activities = new ArrayList<>();
        String color = null;
        long distance = 0;
        long elevation = 0;
        long time = 0;

        for (ActivityMetric progress : progresses) {
            Optional<SportCache> sportOptional = sportCacheService.getOptionalSport(progress.getSportId());
            if (sportOptional.isEmpty()) {
                continue;
            }

            SportCache sport = sportOptional.get();

            activities.add(mapToTrainingLogActivityResponse(progress, sport));

            if (color == null) {
                color = sport.getColor();
            } else {
                color = SportColorConst.DEFAULT;
            }

            distance += progress.getDistance();
            elevation += progress.getElevationGain();
            time += progress.getMovingTimeSeconds();
        }

        // Check whether the date have any progresses
        color = color == null ? SportColorConst.DEFAULT : color;

        return TrainingLogResponse.builder()
                .date(date)
                .activities(activities)
                .color(color)
                .distance(distance)
                .elevation(elevation)
                .time(time)
                .build();
    }

    private TrainingLogActivityResponse mapToTrainingLogActivityResponse(
            ActivityMetric progress,
            SportCache sport
    ) {
        return TrainingLogActivityResponse.builder()
                .id(progress.getActivityId())
                .name(progress.getName())
                .sport(sportMapper.mapToShortResponse(sport))
                .date(DateUtils.toDate(progress.getTime()))
                .distance(progress.getDistance())
                .elevation(progress.getElevationGain())
                .time(progress.getMovingTimeSeconds())
                .build();
    }
}
