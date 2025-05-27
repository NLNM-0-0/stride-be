package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.coreservice.mapper.SportMapper;
import com.stride.tracking.coreservice.model.Progress;
import com.stride.tracking.coreservice.model.Sport;
import com.stride.tracking.coreservice.repository.ProgressRepository;
import com.stride.tracking.coreservice.service.ProgressService;
import com.stride.tracking.coreservice.utils.DateUtils;
import com.stride.tracking.coreservice.utils.ProgressTimeFrameHelper;
import com.stride.tracking.dto.progress.ProgressTimeFrame;
import com.stride.tracking.dto.progress.request.GetProgressActivityRequest;
import com.stride.tracking.dto.progress.request.ProgressFilter;
import com.stride.tracking.dto.progress.response.*;
import com.stride.tracking.dto.sport.response.SportWithMapTypeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {
    private final ProgressRepository progressRepository;

    private final SportCacheService sportCacheService;

    private final SportMapper sportMapper;

    @Override
    @Transactional
    public ProgressDetailResponse getProgress(
            ZoneId zoneId,
            ProgressFilter filter
    ) {
        String userId = SecurityUtils.getCurrentUserId();

        Instant start = ProgressTimeFrameHelper.getAuditStartInstant(zoneId);

        Sport sport = sportCacheService.findSportById(filter.getSportId());

        //Get shared progresses for all time frames
        List<Progress> progresses =
                progressRepository.findAllByUserIdAndSportAndCreatedAtGreaterThanEqual(
                        userId,
                        sport,
                        start
                );

        Map<ProgressTimeFrame, List<ProgressBySportResponse>> progressesByTimeFrame = new ConcurrentHashMap<>();
        AtomicReference<List<SportWithMapTypeResponse>> availableSportsRef = new AtomicReference<>();

        List<CompletableFuture<Void>> futures = List.of(
                CompletableFuture.runAsync(() -> {
                    processTimeFrame(ProgressTimeFrame.YEAR, progresses, zoneId, progressesByTimeFrame);
                    processTimeFrame(ProgressTimeFrame.YEAR_TO_DATE, progresses, zoneId, progressesByTimeFrame);
                }),
                CompletableFuture.runAsync(() -> {
                    processTimeFrame(ProgressTimeFrame.THREE_MONTHS, progresses, zoneId, progressesByTimeFrame);
                    processTimeFrame(ProgressTimeFrame.MONTH, progresses, zoneId, progressesByTimeFrame);
                    processTimeFrame(ProgressTimeFrame.WEEK, progresses, zoneId, progressesByTimeFrame);
                }),
                CompletableFuture.runAsync(() -> {
                    processTimeFrame(ProgressTimeFrame.SIX_MONTHS, progresses, zoneId, progressesByTimeFrame);

                    buildAvailableSport(start, availableSportsRef);
                })
        );

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<SportWithMapTypeResponse> availableSports = availableSportsRef.get();

        return ProgressDetailResponse.builder()
                .sport(sportMapper.mapToWithMapTypeResponse(sport))
                .availableSports(availableSports)
                .progresses(progressesByTimeFrame)
                .build();
    }

    private void processTimeFrame(
            ProgressTimeFrame timeFrame,
            List<Progress> progresses,
            ZoneId zoneId,
            Map<ProgressTimeFrame, List<ProgressBySportResponse>> progressesByTimeFrame
    ) {
        Instant startAuditDate = ProgressTimeFrameHelper.getAuditStartInstant(timeFrame, zoneId);

        List<Progress> filteredProgresses = progresses.stream()
                .filter(progress -> !progress.getCreatedAt().isBefore(startAuditDate))
                .toList();

        List<ProgressBySportResponse> data = buildProgressBySportResponses(
                filteredProgresses,
                timeFrame,
                zoneId
        );

        progressesByTimeFrame.put(timeFrame, data); // No synchronization needed
    }

    private List<ProgressBySportResponse> buildProgressBySportResponses(
            List<Progress> progresses,
            ProgressTimeFrame timeFrame,
            ZoneId zoneId
    ) {
        // Simply return the relevant data by extracting the Instant from each Progress
        // there's no need to generate any template dates
        Map<Instant, List<Progress>> template = new HashMap<>();

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
            List<Progress> progresses,
            Map<Instant, List<Progress>> template,
            ProgressTimeFrame timeFrame,
            ZoneId zoneId) {
        progresses.forEach(progress -> {
            Instant startDate = ProgressTimeFrameHelper.resolveStartDate(
                    progress.getCreatedAt(),
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
            List<Progress> group,
            ZoneId zoneId
    ) {
        long distance = 0;
        long elevation = 0;
        long time = 0;
        long numberActivities = 0;

        for (Progress progress : group) {
            distance += progress.getDistance();
            elevation += progress.getElevation();
            time += progress.getTime();
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
            Instant start,
            AtomicReference<List<SportWithMapTypeResponse>> availableSportsRef
    ){
        List<Sport> availableSport = progressRepository.findDistinctSportsSinceNative(start);

        List<SportWithMapTypeResponse> responses = availableSport.stream()
                .map(sportMapper::mapToWithMapTypeResponse)
                .toList();

        availableSportsRef.set(responses);
    }

    @Override
    @Transactional
    public SimpleListResponse<ProgressResponse> getProgress(
            ZoneId zoneId
    ) {
        String userId = SecurityUtils.getCurrentUserId();

        ProgressTimeFrame timeFrame = ProgressTimeFrame.THREE_MONTHS; //default

        Instant start = ProgressTimeFrameHelper.getAuditStartInstant(
                timeFrame,
                zoneId
        );

        List<Progress> progresses =
                progressRepository.findAllByUserIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
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
            List<Progress> progresses,
            ProgressTimeFrame timeFrame,
            ZoneId zoneId
    ) {
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
                                new HashMap<>()
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

        SportWithMapTypeResponse sportResponse = sportMapper.mapToWithMapTypeResponse(sport);

        return ProgressResponse.builder()
                .sport(sportResponse)
                .progresses(progresses)
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
                        DateUtils.toInstant(request.getFromDate()),
                        DateUtils.toInstant(request.getToDate())
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
