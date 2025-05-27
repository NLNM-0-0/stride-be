package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.dto.ListWithMetadataResponse;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.coreservice.mapper.TrainingLogMapper;
import com.stride.tracking.coreservice.model.Progress;
import com.stride.tracking.coreservice.repository.ProgressRepository;
import com.stride.tracking.coreservice.service.TrainingLogService;
import com.stride.tracking.coreservice.utils.DateUtils;
import com.stride.tracking.dto.traininglog.request.TrainingLogFilter;
import com.stride.tracking.dto.traininglog.response.TrainingLogMetadata;
import com.stride.tracking.dto.traininglog.response.TrainingLogResponse;
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
    private final ProgressRepository progressRepository;

    private final TrainingLogMapper trainingLogMapper;

    @Override
    @Transactional
    public ListWithMetadataResponse<TrainingLogResponse, TrainingLogFilter, TrainingLogMetadata> getTrainingLogs(
            TrainingLogFilter filter,
            ZoneId zoneId
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
        Map<Date, List<Progress>> dates = new HashMap<>();

        List<Progress> progresses = findProgress(filter);

        for (Progress progress : progresses) {
            Date date = DateUtils.toStartDate(progress.getCreatedAt(), zoneId);

            dates.computeIfAbsent(date, k -> new ArrayList<>())
                    .add(progress);
        }

        List<TrainingLogResponse> data = new ArrayList<>();
        for (Map.Entry<Date, List<Progress>> entry : dates.entrySet()) {
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

    private List<Progress> findProgress(TrainingLogFilter filter) {
        Instant fromInstant = DateUtils.toInstant(filter.getFromDate());
        Instant toInstant = DateUtils.toInstant(filter.getToDate());

        return Optional.ofNullable(filter.getSportIds())
                .map(sportIds -> progressRepository.findAllByUserIdAndSport_IdInAndCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(
                        filter.getUserId(),
                        sportIds,
                        fromInstant,
                        toInstant
                ))
                .orElse(progressRepository.findAllByUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(
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

        Object[] result = progressRepository.findMinAndMaxCreatedAtByUserId(userId);
        Instant min = (Instant) result[0];
        Instant max = (Instant) result[1];

        return TrainingLogMetadata.builder()
                .from(DateUtils.toStartDate(min, zoneId))
                .to(DateUtils.toEndDate(max, zoneId))
                .build();
    }
}
