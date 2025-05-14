package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.coreservice.constant.ProgressCountType;
import com.stride.tracking.coreservice.constant.ProgressTimeFrame;
import com.stride.tracking.coreservice.constant.ProgressType;
import com.stride.tracking.coreservice.dto.progress.request.GetProgressActivityRequest;
import com.stride.tracking.coreservice.dto.progress.request.ProgressFilter;
import com.stride.tracking.coreservice.dto.progress.response.*;
import com.stride.tracking.coreservice.dto.sport.response.SportShortResponse;
import com.stride.tracking.coreservice.mapper.SportMapper;
import com.stride.tracking.coreservice.model.Progress;
import com.stride.tracking.coreservice.model.Sport;
import com.stride.tracking.coreservice.repository.ProgressRepository;
import com.stride.tracking.coreservice.service.ProgressService;
import com.stride.tracking.coreservice.utils.ProgressTimeFrameHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {
    private final ProgressRepository progressRepository;

    private final SportMapper sportMapper;

    @Override
    @Transactional
    public SimpleListResponse<ProgressShortResponse> getProgress(
            ZoneId zoneId,
            ProgressFilter filter
    ) {
        String userId = SecurityUtils.getCurrentUserId();

        Calendar startTime = ProgressTimeFrameHelper.getAuditStartCalendar(
                filter.getTimeFrame(),
                zoneId
        );

        List<Progress> progresses =
                progressRepository.findAllByUserIdAndSport_IdAndCreatedAtGreaterThanEqual(
                        userId,
                        filter.getSportId(),
                        startTime.toInstant()
                );

        List<ProgressShortResponse> data = buildProgressShortResponses(
                progresses,
                filter.getTimeFrame(),
                filter.getType(),
                zoneId
        );

        return SimpleListResponse.<ProgressShortResponse>builder()
                .data(data)
                .build();
    }

    private List<ProgressShortResponse> buildProgressShortResponses(
            List<Progress> progresses,
            ProgressTimeFrame timeFrame,
            ProgressType type,
            ZoneId zoneId
    ) {
        ProgressCountType countType = timeFrame.getCountType();

        return progresses.stream()
                .collect(
                        Collectors.groupingBy(progress ->
                                toGroupKey(
                                        progress.getCreatedAt(),
                                        countType,
                                        zoneId
                                )
                        )
                )
                .entrySet().stream()
                .map(entry ->
                        toProgressShortResponse(
                                entry.getKey(),
                                entry.getValue(),
                                type,
                                countType,
                                zoneId
                        )
                )
                .sorted(Comparator.comparing(ProgressShortResponse::getFromDate))
                .toList();
    }

    private LocalDate toGroupKey(
            Instant createdAt,
            ProgressCountType countType,
            ZoneId zoneId
    ) {
        LocalDate date = createdAt.atZone(zoneId).toLocalDate();
        return countType == ProgressCountType.DAILY ? date : date.with(DayOfWeek.MONDAY);
    }

    private ProgressShortResponse toProgressShortResponse(
            LocalDate start,
            List<Progress> group,
            ProgressType type,
            ProgressCountType countType,
            ZoneId zoneId
    ) {
        long numActivities = group.size();
        long amount = calculateAmount(group, type);
        LocalDate end = countType == ProgressCountType.DAILY ? start : start.plusDays(6);

        return ProgressShortResponse.builder()
                .numberActivities(numActivities)
                .amount(amount)
                .fromDate(toStartOfDay(start, zoneId))
                .toDate(toEndOfDay(end, zoneId))
                .build();
    }

    private Date toStartOfDay(LocalDate date, ZoneId zoneId) {
        return Date.from(date.atStartOfDay(zoneId).toInstant());
    }

    private Date toEndOfDay(LocalDate date, ZoneId zoneId) {
        return Date.from(date.atTime(LocalTime.MAX).atZone(zoneId).toInstant());
    }

    private long calculateAmount(List<Progress> group, ProgressType type) {
        return switch (type) {
            case DISTANCE -> group.stream()
                    .mapToLong(Progress::getDistance)
                    .sum();
            case ACTIVITY -> group.size();
            case ELEVATION -> group.stream()
                    .mapToLong(Progress::getElevation)
                    .sum();
            case TIME -> group.stream()
                    .mapToLong(Progress::getTime)
                    .sum();
        };
    }

    @Override
    @Transactional
    public SimpleListResponse<ProgressResponse> getProgress(
            ZoneId zoneId
    ) {
        String userId = SecurityUtils.getCurrentUserId();

        ProgressTimeFrame timeFrame = ProgressTimeFrame.THREE_MONTHS; //default

        Calendar startTime = ProgressTimeFrameHelper.getAuditStartCalendar(
                timeFrame,
                zoneId
        );

        List<Progress> progresses =
                progressRepository.findAllByUserIdAndCreatedAtGreaterThanEqual(
                        userId,
                        startTime.toInstant()
                );

        List<ProgressResponse> data = buildProgressResponses(
                progresses,
                timeFrame,
                zoneId
        );

        return SimpleListResponse.<ProgressResponse>builder()
                .data(data)
                .build();
    }

    private List<ProgressResponse> buildProgressResponses(
            List<Progress> progresses,
            ProgressTimeFrame timeFrame,
            ZoneId zoneId
    ) {
        ProgressCountType countType = timeFrame.getCountType();

        return progresses.stream()
                .collect(Collectors.groupingBy(Progress::getSport))
                .entrySet().stream()
                .map(entry ->
                        buildProgressResponse(
                                entry.getKey(),
                                entry.getValue(),
                                countType,
                                zoneId
                        )
                )
                .toList();
    }

    private ProgressResponse buildProgressResponse(
            Sport sport,
            List<Progress> sportProgresses,
            ProgressCountType countType,
            ZoneId zoneId
    ) {

        Map<LocalDate, List<Progress>> groupedByTime = sportProgresses.stream()
                .collect(Collectors.groupingBy(progress -> toGroupKey(progress.getCreatedAt(), countType, zoneId)));

        List<ProgressBySportResponse> progressList = groupedByTime.entrySet().stream()
                .map(entry -> buildProgressBySportResponse(entry.getKey(), entry.getValue(), countType, zoneId))
                .sorted(Comparator.comparing(ProgressBySportResponse::getFromDate))
                .toList();

        SportShortResponse sportResponse = sportMapper.mapToShortResponse(sport);
        return ProgressResponse.builder()
                .sport(sportResponse)
                .progress(progressList)
                .build();
    }

    private ProgressBySportResponse buildProgressBySportResponse(
            LocalDate start,
            List<Progress> group,
            ProgressCountType countType,
            ZoneId zoneId
    ) {
        long numActivities = group.size();
        long distance = group.stream().mapToLong(Progress::getDistance).sum();
        long elevation = group.stream().mapToLong(Progress::getElevation).sum();
        long time = group.stream().mapToLong(Progress::getTime).sum();
        LocalDate end = countType == ProgressCountType.DAILY ? start : start.plusDays(6);

        return ProgressBySportResponse.builder()
                .fromDate(toStartOfDay(start, zoneId))
                .toDate(toEndOfDay(end, zoneId))
                .distance(distance)
                .elevation(elevation)
                .time(time)
                .numberActivities(numActivities)
                .build();
    }


    @Override
    public GetProgressActivityResponse getProgressActivity(
            GetProgressActivityRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();

        List<Progress> progresses =
                progressRepository.findAllByUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(
                        userId,
                        request.getFromDate().toInstant(),
                        request.getToDate().toInstant()
                );

        long totalDistance = 0;
        long totalTime = 0;
        long totalElevation = 0;
        List<ProgressActivityResponse> activities = new ArrayList<>();

        for (Progress progress : progresses) {
            totalDistance += progress.getDistance();
            totalTime += progress.getTime();
            totalElevation += progress.getElevation();
            activities.add(ProgressActivityResponse.builder()
                    .distance(progress.getDistance())
                    .elevation(progress.getElevation())
                    .time(progress.getTime())
                    .mapImage(progress.getActivity().getMapImage())
                    .name(progress.getActivity().getName())
                    .build());
        }

        return GetProgressActivityResponse.builder()
                .activities(activities)
                .distance(totalDistance)
                .time(totalTime)
                .elevation(totalElevation)
                .build();
    }
}
