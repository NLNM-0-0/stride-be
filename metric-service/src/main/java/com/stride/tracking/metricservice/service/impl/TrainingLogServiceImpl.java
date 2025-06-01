package com.stride.tracking.metricservice.service.impl;

import com.stride.tracking.commons.dto.ListWithMetadataResponse;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.DateUtils;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.metric.dto.traininglog.request.TrainingLogFilter;
import com.stride.tracking.metric.dto.traininglog.response.TrainingLogMetadata;
import com.stride.tracking.metric.dto.traininglog.response.TrainingLogResponse;
import com.stride.tracking.metricservice.constant.Message;
import com.stride.tracking.metricservice.dto.progress.FindMinAndMaxTimeByUserIdResult;
import com.stride.tracking.metricservice.mapper.TrainingLogMapper;
import com.stride.tracking.metricservice.model.ActivityMetric;
import com.stride.tracking.metricservice.repository.ActivityMetricRepository;
import com.stride.tracking.metricservice.service.TrainingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TrainingLogServiceImpl implements TrainingLogService {
    private final ActivityMetricRepository activityMetricRepository;

    private final TrainingLogMapper trainingLogMapper;

    @Override
    @Transactional(readOnly = true)
    public ListWithMetadataResponse<TrainingLogResponse, TrainingLogFilter, TrainingLogMetadata> getTrainingLogs(
            ZoneId zoneId,
            TrainingLogFilter filter
    ) {
        initializeAndValidateFilter(filter, zoneId);

        return findTrainingLogs(filter, zoneId);
    }

    private void initializeAndValidateFilter(
            TrainingLogFilter filter,
            ZoneId zoneId
    ) {
        filter.setUserId(SecurityUtils.getCurrentUserId());

        if (filter.getFromDate() == null && filter.getToDate() == null) {
            filter.setFromDate(DateUtils.getStartOfWeekDate(zoneId));
            filter.setToDate(DateUtils.getEndOfWeekDate(zoneId));
            filter.setSportIds(null);
        } else if (filter.getFromDate() == null || filter.getToDate() == null) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.TRAINING_LOG_FILTER_VALIDATE);
        }
    }

    private ListWithMetadataResponse<TrainingLogResponse, TrainingLogFilter, TrainingLogMetadata> findTrainingLogs(
            TrainingLogFilter filter,
            ZoneId zoneId
    ) {
        Map<Date, List<ActivityMetric>> dates = new HashMap<>();

        List<ActivityMetric> progresses = findProgress(filter);

        for (ActivityMetric progress : progresses) {
            Date date = DateUtils.toStartDate(progress.getTime(), zoneId);

            dates.computeIfAbsent(date, k -> new ArrayList<>())
                    .add(progress);
        }

        List<TrainingLogResponse> data = new ArrayList<>();
        for (Map.Entry<Date, List<ActivityMetric>> entry : dates.entrySet()) {
            TrainingLogResponse trainingLog = trainingLogMapper.mapToTrainingLogResponse(
                    entry.getValue(),
                    entry.getKey()
            );
            data.add(trainingLog);
        }

        return ListWithMetadataResponse.<TrainingLogResponse, TrainingLogFilter, TrainingLogMetadata>builder()
                .data(data)
                .filter(filter)
                .metadata(findTrainingLogMetadata(filter.getUserId(), zoneId))
                .build();
    }

    private List<ActivityMetric> findProgress(TrainingLogFilter filter) {
        Instant fromInstant = DateUtils.toInstant(filter.getFromDate());
        Instant toInstant = DateUtils.toInstant(filter.getToDate());

        return Optional.ofNullable(filter.getSportIds())
                .map(sportIds -> activityMetricRepository.findAllByUserIdAndSportIdInAndTimeGreaterThanEqualAndTimeLessThanEqual(
                        filter.getUserId(),
                        sportIds,
                        fromInstant,
                        toInstant
                ))
                .orElse(activityMetricRepository.findAllByUserIdAndTimeGreaterThanEqualAndTimeLessThanEqual(
                        filter.getUserId(),
                        fromInstant,
                        toInstant
                ));
    }

    private TrainingLogMetadata findTrainingLogMetadata(
            String userId,
            ZoneId zoneId
    ) {
        if (userId == null) {
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.CAN_NOT_FIND_USER_ID);
        }

        Optional<Instant> min = Optional.empty();
        Optional<Instant> max = Optional.empty();
        try {
            FindMinAndMaxTimeByUserIdResult result = activityMetricRepository.findMinAndMaxTimeByUserId(userId);
            min = Optional.of(result.getMin());
            max = Optional.of(result.getMax());
        } catch (Exception e) {
            System.out.println(e);
        }

        return TrainingLogMetadata.builder()
                .from(DateUtils.toStartDate(min.orElse(Instant.now()), zoneId))
                .to(DateUtils.toEndDate(max.orElse(Instant.now()), zoneId))
                .build();
    }
}
