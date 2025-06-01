package com.stride.tracking.metricservice.service.impl;

import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.utils.DateUtils;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.commons.utils.TaskHelper;
import com.stride.tracking.metric.dto.progress.ProgressTimeFrame;
import com.stride.tracking.metric.dto.progress.request.GetProgressActivityRequest;
import com.stride.tracking.metric.dto.progress.request.ProgressFilter;
import com.stride.tracking.metric.dto.progress.response.*;
import com.stride.tracking.metric.dto.sport.response.SportShortResponse;
import com.stride.tracking.metricservice.mapper.SportCacheMapper;
import com.stride.tracking.metricservice.model.ActivityMetric;
import com.stride.tracking.metricservice.model.SportCache;
import com.stride.tracking.metricservice.repository.ActivityMetricRepository;
import com.stride.tracking.metricservice.service.ProgressService;
import com.stride.tracking.metricservice.utils.ProgressTimeFrameHelper;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {
    private final ActivityMetricRepository activityMetricRepository;

    private final SportCacheService sportCacheService;

    private final SportCacheMapper sportMapper;

    private final Executor asyncExecutor;
    private final Tracer tracer;

    @Override
    @Transactional(readOnly = true)
    public ProgressDetailResponse getProgress(
            ZoneId zoneId,
            ProgressFilter filter
    ) {
        String userId = SecurityUtils.getCurrentUserId();

        SportCache sport = sportCacheService.getSport(filter.getSportId());

        Instant start = ProgressTimeFrameHelper.getAuditStartInstant(zoneId);

        //Get shared progresses for all time frames
        List<ActivityMetric> progresses =
                activityMetricRepository.findAllByUserIdAndSportIdAndTimeGreaterThanEqual(
                        userId,
                        filter.getSportId(),
                        start
                );

        Map<ProgressTimeFrame, List<ProgressBySportResponse>> progressesByTimeFrame = new ConcurrentHashMap<>();
        AtomicReference<List<SportShortResponse>> availableSportsRef = new AtomicReference<>();

        Span parent = tracer.currentSpan();
        List<CompletableFuture<Void>> futures = List.of(
                runAsyncWithSpan("process-time-frame-for-YTD-and-1Y", parent, () -> {
                    processTimeFrame(ProgressTimeFrame.YEAR, progresses, zoneId, progressesByTimeFrame);
                    processTimeFrame(ProgressTimeFrame.YEAR_TO_DATE, progresses, zoneId, progressesByTimeFrame);
                }),
                runAsyncWithSpan("process-time-frame-for-3M-and-1M-and-7D", parent, () -> {
                    processTimeFrame(ProgressTimeFrame.THREE_MONTHS, progresses, zoneId, progressesByTimeFrame);
                    processTimeFrame(ProgressTimeFrame.MONTH, progresses, zoneId, progressesByTimeFrame);
                    processTimeFrame(ProgressTimeFrame.WEEK, progresses, zoneId, progressesByTimeFrame);
                }),
                runAsyncWithSpan("process-time-frame-for-6M-and-build-available-sports", parent, () -> {
                    processTimeFrame(ProgressTimeFrame.SIX_MONTHS, progresses, zoneId, progressesByTimeFrame);

                    buildAvailableSport(userId, start, availableSportsRef);
                })
        );

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<SportShortResponse> availableSports = availableSportsRef.get();

        return ProgressDetailResponse.builder()
                .sport(sportMapper.mapToShortResponse(sport))
                .availableSports(availableSports)
                .progresses(progressesByTimeFrame)
                .build();
    }

    private CompletableFuture<Void> runAsyncWithSpan(String spanName, Span parent, Runnable task) {
        return TaskHelper.runAsyncWithSpan(
                spanName,
                parent,
                task,
                asyncExecutor,
                tracer
        );
    }


    private void processTimeFrame(
            ProgressTimeFrame timeFrame,
            List<ActivityMetric> progresses,
            ZoneId zoneId,
            Map<ProgressTimeFrame, List<ProgressBySportResponse>> progressesByTimeFrame
    ) {
        Instant startAuditDate = ProgressTimeFrameHelper.getAuditStartInstant(timeFrame, zoneId);

        List<ActivityMetric> filteredProgresses = progresses.stream()
                .filter(progress -> !progress.getTime().isBefore(startAuditDate))
                .toList();

        List<ProgressBySportResponse> data = buildProgressBySportResponses(
                filteredProgresses,
                timeFrame,
                zoneId
        );

        progressesByTimeFrame.put(timeFrame, data); // No synchronization needed
    }

    private List<ProgressBySportResponse> buildProgressBySportResponses(
            List<ActivityMetric> progresses,
            ProgressTimeFrame timeFrame,
            ZoneId zoneId
    ) {
        // Simply return the relevant data by extracting the Instant from each Progress
        // there's no need to generate any template dates
        Map<Instant, List<ActivityMetric>> template = new HashMap<>();

        loadProgressToMap(progresses, template, timeFrame, zoneId);

        return template.entrySet().stream()
                .map(entry -> buildProgressBySportResponse(
                        entry.getKey(),
                        timeFrame,
                        entry.getValue(),
                        zoneId
                ))
                .sorted(Comparator.comparing(ProgressBySportResponse::getFromDate))
                .toList();
    }

    private void loadProgressToMap(
            List<ActivityMetric> progresses,
            Map<Instant, List<ActivityMetric>> template,
            ProgressTimeFrame timeFrame,
            ZoneId zoneId) {
        progresses.forEach(progress -> {
            Instant startDate = ProgressTimeFrameHelper.resolveStartDate(
                    progress.getTime(),
                    timeFrame,
                    zoneId
            );
            template.computeIfAbsent(startDate, k -> new ArrayList<>())
                    .add(progress);
        });
    }

    private ProgressBySportResponse buildProgressBySportResponse(
            Instant start,
            ProgressTimeFrame timeFrame,
            List<ActivityMetric> group,
            ZoneId zoneId
    ) {
        long distance = 0;
        long elevation = 0;
        long time = 0;
        long numberActivities = 0;

        for (ActivityMetric progress : group) {
            distance += progress.getDistance();
            elevation += progress.getElevationGain();
            time += progress.getMovingTimeSeconds();
            numberActivities++;
        }

        Date fromDate = DateUtils.toStartDate(start, zoneId);
        Date toDate = Date.from(ProgressTimeFrameHelper.resolveEndDate(start, timeFrame, zoneId));

        return ProgressBySportResponse.builder()
                .distance(distance)
                .time(time)
                .elevation(elevation)
                .numberActivities(numberActivities)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();
    }

    private void buildAvailableSport(
            String userId,
            Instant start,
            AtomicReference<List<SportShortResponse>> availableSportsRef
    ) {
        List<String> availableSport = activityMetricRepository.findDistinctSportsSinceNative(userId, start);

        List<SportShortResponse> responses = availableSport.stream()
                .map(sportCacheService::getSport)
                .map(sportMapper::mapToShortResponse)
                .toList();

        availableSportsRef.set(responses);
    }

    @Override
    @Transactional(readOnly = true)
    public SimpleListResponse<ProgressResponse> getProgress(
            ZoneId zoneId
    ) {
        String userId = SecurityUtils.getCurrentUserId();

        ProgressTimeFrame timeFrame = ProgressTimeFrame.THREE_MONTHS; //default

        Instant start = ProgressTimeFrameHelper.getAuditStartInstant(
                timeFrame,
                zoneId
        );

        List<ActivityMetric> progresses =
                activityMetricRepository.findAllByUserIdAndTimeGreaterThanEqualOrderByTimeDesc(
                        userId,
                        start
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
            List<ActivityMetric> progresses,
            ProgressTimeFrame timeFrame,
            ZoneId zoneId
    ) {
        return progresses.stream()
                .collect(Collectors.groupingBy(
                        ActivityMetric::getSportId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(entry -> toProgressResponse(
                        entry.getKey(),
                        entry.getValue(),
                        timeFrame,
                        zoneId,
                        new HashMap<>()
                ))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }


    private Optional<ProgressResponse> toProgressResponse(
            String sportId,
            List<ActivityMetric> sportProgresses,
            ProgressTimeFrame timeFrame,
            ZoneId zoneId,
            Map<Instant, List<ActivityMetric>> template
    ) {
        loadProgressToMap(sportProgresses, template, timeFrame, zoneId);

        List<ProgressBySportResponse> progresses = template.entrySet().stream()
                .map(entry ->
                        buildProgressBySportResponse(
                                entry.getKey(),
                                timeFrame,
                                entry.getValue(),
                                zoneId
                        )
                )
                .sorted(Comparator.comparing(ProgressBySportResponse::getFromDate))
                .toList();

        Optional<SportCache> sportOptional = sportCacheService.getOptionalSport(sportId);
        if (sportOptional.isEmpty()) {
            return Optional.empty();
        }
        SportShortResponse sportResponse = sportMapper.mapToShortResponse(sportOptional.get());

        return Optional.of(
                ProgressResponse.builder()
                        .sport(sportResponse)
                        .progresses(progresses)
                        .build()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public GetProgressActivityResponse getProgressActivity(
            GetProgressActivityRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();

        List<ActivityMetric> progresses =
                activityMetricRepository.findAllByUserIdAndSportIdAndTimeGreaterThanEqualAndTimeLessThanEqual(
                        userId,
                        request.getSportId(),
                        DateUtils.toInstant(request.getFromDate()),
                        DateUtils.toInstant(request.getToDate())
                );

        long totalDistance = 0;
        long totalTime = 0;
        long totalElevation = 0;
        List<ProgressActivityResponse> activities = new ArrayList<>();

        for (ActivityMetric progress : progresses) {
            totalDistance += progress.getDistance();
            totalTime += progress.getMovingTimeSeconds();
            totalElevation += progress.getElevationGain();
            activities.add(
                    ProgressActivityResponse.builder()
                            .id(progress.getActivityId())
                            .distance(progress.getDistance())
                            .elevation(progress.getElevationGain())
                            .time(progress.getMovingTimeSeconds())
                            .mapImage(progress.getMapImage())
                            .name(progress.getName())
                            .createdAt(DateUtils.toDate(progress.getTime()))
                            .build()
            );
        }

        return GetProgressActivityResponse.builder()
                .activities(activities)
                .distance(totalDistance)
                .time(totalTime)
                .elevation(totalElevation)
                .build();
    }
}
