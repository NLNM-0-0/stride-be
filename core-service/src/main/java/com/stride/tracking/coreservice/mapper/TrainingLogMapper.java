package com.stride.tracking.coreservice.mapper;

import com.stride.tracking.coreservice.constant.SportColorConst;
import com.stride.tracking.coreservice.model.Progress;
import com.stride.tracking.coreservice.utils.DateUtils;
import com.stride.tracking.dto.traininglog.response.TrainingLogActivityResponse;
import com.stride.tracking.dto.traininglog.response.TrainingLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TrainingLogMapper {
    private final SportMapper sportMapper;

    public TrainingLogResponse mapToTrainingLogResponse(
            List<Progress> progresses,
            Date date
    ) {
        List<TrainingLogActivityResponse> activities = new ArrayList<>();
        String color = null;
        long distance = 0;
        long elevation = 0;
        long time = 0;

        for (Progress progress : progresses) {
            activities.add(mapToTrainingLogActivityResponse(progress));

            if (color == null) {
                color = progress.getSport().getColor();
            } else {
                color = SportColorConst.DEFAULT;
            }

            distance += progress.getDistance();
            elevation += progress.getElevation();
            time += progress.getTime();
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
            Progress progress
    ) {
        return TrainingLogActivityResponse.builder()
                .id(progress.getActivity().getId())
                .name(progress.getActivity().getName())
                .sport(sportMapper.mapToShortResponse(progress.getSport()))
                .date(DateUtils.toDate(progress.getActivity().getCreatedAt()))
                .distance(progress.getDistance())
                .elevation(progress.getElevation())
                .time(progress.getTime())
                .build();
    }
}
