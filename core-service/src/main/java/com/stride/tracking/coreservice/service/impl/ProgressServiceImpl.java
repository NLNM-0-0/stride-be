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
import com.stride.tracking.coreservice.utils.InstantUtils;
import com.stride.tracking.coreservice.utils.ProgressTimeFrameHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
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
                startTime,
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
            Calendar startCalendar,
            ZoneId zoneId
    ) {
        ProgressCountType countType = timeFrame.getCountType();
        Map<Instant, List<Progress>> groupedProgress = initializeTemplate(timeFrame, zoneId, startCalendar);

        loadProgressToMap(progresses, groupedProgress, zoneId);

        return groupedProgress.entrySet().stream()
                .map(entry -> toProgressShortResponse(
                        entry.getKey(),
                        entry.getValue(),
                        type,
                        countType,
                        zoneId
                ))
                .sorted(Comparator.comparing(ProgressShortResponse::getFromDate))
                .toList();
    }

    private Map<Instant, List<Progress>> initializeTemplate(ProgressTimeFrame timeFrame, ZoneId zoneId, Calendar startCalendar) {
        int daysInTimeFrame = (timeFrame.getCountType() == ProgressCountType.DAILY) ? 1 : 7;

        Map<Instant, List<Progress>> template = new HashMap<>();
        Instant startInstant = startCalendar.toInstant().atZone(zoneId).toInstant();
        Instant endInstant = LocalDate.now().atTime(LocalTime.MAX).atZone(zoneId).toInstant();

        for (Instant date = startInstant;
             date.isBefore(endInstant);
             date = date.plus(Duration.ofDays(daysInTimeFrame))
        ) {
            template.putIfAbsent(date, new ArrayList<>());
        }

        return template;
    }

    private void loadProgressToMap(
            List<Progress> progresses,
            Map<Instant, List<Progress>> template,
            ZoneId zoneId) {
        progresses.forEach(progress -> {
            Instant startDate = InstantUtils.calculateStartDateInstant(
                    progress.getCreatedAt(),
                    zoneId
            );
            template.computeIfAbsent(startDate, k -> new ArrayList<>())
                    .add(progress);
        });
    }

    private ProgressShortResponse toProgressShortResponse(
            Instant start,
            List<Progress> group,
            ProgressType type,
            ProgressCountType countType,
            ZoneId zoneId
    ) {
        long numActivities = group.size();
        long amount = calculateAmount(group, type);

        Instant end = countType == ProgressCountType.DAILY
                ? start
                : start.plus(6, ChronoUnit.DAYS);

        return ProgressShortResponse.builder()
                .numberActivities(numActivities)
                .amount(amount)
                .fromDate(toStartOfDay(start, zoneId))
                .toDate(toEndOfDay(end, zoneId))
                .build();
    }

    private long calculateAmount(
            List<Progress> group,
            ProgressType type
    ) {
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

    private Date toStartOfDay(
            Instant date,
            ZoneId zoneId
    ) {
        return Date.from(
                date.atZone(zoneId)
                        .toLocalDate()
                        .atStartOfDay(zoneId)
                        .toInstant()
        );
    }

    private Date toEndOfDay(
            Instant date,
            ZoneId zoneId
    ) {
        return Date.from(
                date.atZone(zoneId)
                        .toLocalDate()
                        .atTime(LocalTime.MAX)
                        .atZone(zoneId)
                        .toInstant()
        );
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
                progressRepository.findAllByUserIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
                        userId,
                        startTime.toInstant()
                );

        List<ProgressResponse> data = buildProgressResponses(
                progresses,
                timeFrame,
                zoneId,
                startTime
        );

        return SimpleListResponse.<ProgressResponse>builder()
                .data(data)
                .build();
    }

    private List<ProgressResponse> buildProgressResponses(
            List<Progress> progresses,
            ProgressTimeFrame timeFrame,
            ZoneId zoneId,
            Calendar startCalendar
    ) {
        Map<Instant, List<Progress>> template = initializeTemplate(
                timeFrame,
                zoneId,
                startCalendar
        );

        return progresses.stream()
                .collect(Collectors.groupingBy(
                        Progress::getSport,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(entry ->
                        toProgressResponse(
                                entry.getKey(),
                                entry.getValue(),
                                timeFrame,
                                zoneId,
                                new HashMap<>(template)
                        )
                )
                .toList();
    }

    private ProgressResponse toProgressResponse(
            Sport sport,
            List<Progress> sportProgresses,
            ProgressTimeFrame timeFrame,
            ZoneId zoneId,
            Map<Instant, List<Progress>> template
    ) {
        loadProgressToMap(sportProgresses, template, zoneId);

        List<ProgressBySportResponse> progressList = template.entrySet().stream()
                .map(entry ->
                        toProgressBySportResponse(
                                entry.getKey(),
                                entry.getValue(),
                                timeFrame.getCountType(),
                                zoneId
                        )
                )
                .sorted(Comparator.comparing(ProgressBySportResponse::getFromDate))
                .toList();

        SportShortResponse sportResponse = sportMapper.mapToShortResponse(sport);

        return ProgressResponse.builder()
                .sport(sportResponse)
                .progress(progressList)
                .build();
    }

    private ProgressBySportResponse toProgressBySportResponse(
            Instant start,
            List<Progress> group,
            ProgressCountType countType,
            ZoneId zoneId
    ) {
        long numActivities = group.size();
        long distance = group.stream().mapToLong(Progress::getDistance).sum();
        long elevation = group.stream().mapToLong(Progress::getElevation).sum();
        long time = group.stream().mapToLong(Progress::getTime).sum();

        Instant end = countType == ProgressCountType.DAILY
                ? start
                : start.plus(6, ChronoUnit.DAYS);

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
                progressRepository.findAllByUserIdAndSport_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(
                        userId,
                        request.getSportId(),
                        Instant.ofEpochMilli(request.getFromDate()),
                        Instant.ofEpochMilli(request.getToDate())
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
                    .id(progress.getActivity().getId())
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
