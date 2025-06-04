package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.configuration.kafka.KafkaProducer;
import com.stride.tracking.commons.constants.KafkaTopics;
import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.commons.dto.page.AppPageResponse;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.DateUtils;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.commons.utils.TaskHelper;
import com.stride.tracking.commons.utils.UpdateHelper;
import com.stride.tracking.core.dto.activity.request.ActivityFilter;
import com.stride.tracking.core.dto.activity.request.CoordinateRequest;
import com.stride.tracking.core.dto.activity.request.CreateActivityRequest;
import com.stride.tracking.core.dto.activity.request.UpdateActivityRequest;
import com.stride.tracking.core.dto.activity.response.ActivityResponse;
import com.stride.tracking.core.dto.activity.response.ActivityShortResponse;
import com.stride.tracking.core.dto.goal.GoalType;
import com.stride.tracking.core.dto.route.request.CreateRouteRequest;
import com.stride.tracking.core.dto.route.request.UpdateRouteRequest;
import com.stride.tracking.core.dto.route.response.CreateRouteResponse;
import com.stride.tracking.core.dto.sport.SportMapType;
import com.stride.tracking.core.dto.supabase.request.GeometryRequest;
import com.stride.tracking.core.dto.supabase.request.GetLocationByGeometryRequest;
import com.stride.tracking.core.dto.supabase.response.GetLocationByGeometryResponse;
import com.stride.tracking.coreservice.constant.GeometryType;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.coreservice.constant.RoundRules;
import com.stride.tracking.coreservice.constant.RuleCaloriesType;
import com.stride.tracking.coreservice.mapper.ActivityMapper;
import com.stride.tracking.coreservice.model.*;
import com.stride.tracking.coreservice.repository.ActivityRepository;
import com.stride.tracking.coreservice.repository.GoalHistoryRepository;
import com.stride.tracking.coreservice.repository.GoalRepository;
import com.stride.tracking.coreservice.repository.SportRepository;
import com.stride.tracking.coreservice.repository.specs.ActivitySpecs;
import com.stride.tracking.coreservice.service.ActivityService;
import com.stride.tracking.coreservice.utils.*;
import com.stride.tracking.coreservice.utils.calculator.CaloriesCalculator;
import com.stride.tracking.coreservice.utils.calculator.CarbonSavedCalculator;
import com.stride.tracking.coreservice.utils.calculator.RamerDouglasPeucker;
import com.stride.tracking.coreservice.utils.calculator.elevation.ElevationCalculator;
import com.stride.tracking.coreservice.utils.calculator.elevation.ElevationCalculatorResult;
import com.stride.tracking.coreservice.utils.calculator.heartrate.HeartRateCalculator;
import com.stride.tracking.coreservice.utils.calculator.heartrate.HeartRateCalculatorResult;
import com.stride.tracking.coreservice.utils.calculator.speed.SpeedCalculator;
import com.stride.tracking.coreservice.utils.calculator.speed.SpeedCalculatorResult;
import com.stride.tracking.metric.dto.activity.event.ActivityCreatedEvent;
import com.stride.tracking.metric.dto.activity.event.ActivityDeletedEvent;
import com.stride.tracking.metric.dto.activity.event.ActivityUpdatedEvent;
import com.stride.tracking.profile.dto.profile.response.ProfileResponse;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityServiceImpl implements ActivityService {
    private final ActivityRepository activityRepository;
    private final GoalRepository goalRepository;
    private final GoalHistoryRepository goalHistoryRepository;

    private final SportRepository sportRepository;

    private final MapboxService mapboxService;
    private final SupabaseService supabaseService;
    private final RouteServiceImpl routeServiceImpl;
    private final ProfileService profileService;
    private final ElevationService elevationService;

    private final ElevationCalculator elevationCalculator;
    private final HeartRateCalculator heartRateCalculator;
    private final SpeedCalculator speedCalculator;
    private final CarbonSavedCalculator carbonSavedCalculator;
    private final CaloriesCalculator caloriesCalculator;

    private final ActivityMapper activityMapper;

    private final KafkaProducer kafkaProducer;

    private final Executor asyncExecutor;
    private final Tracer tracer;

    @PersistenceContext
    private EntityManager entityManager;

    private static final double RDP_EPSILON = 0.00005;
    private static final int NUMBER_CHART_POINTS = 100;
    private static final int THRESHOLD_METERS = 10;

    @Override
    @Transactional(readOnly = true)
    public ListResponse<ActivityShortResponse, ActivityFilter> getActivitiesOfUser(
            ZoneId zoneId,
            AppPageRequest page,
            ActivityFilter filter
    ) {
        ProfileResponse userResponse = profileService.viewProfile();

        filter.setUserId(userResponse.getId());

        Pageable pageable = PageRequest.of(
                page.getPage() - 1,
                page.getLimit(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Specification<Activity> spec = filterActivities(zoneId, filter);

        Page<Activity> activityPage = activityRepository.findAll(spec, pageable);

        List<Activity> activities = activityPage.getContent();

        List<ActivityShortResponse> data = activities.stream().map(activity -> activityMapper.mapToShortResponse(
                activity,
                userResponse
        )).toList();

        return ListResponse.<ActivityShortResponse, ActivityFilter>builder()
                .data(data)
                .appPageResponse(AppPageResponse.builder()
                        .index(page.getPage())
                        .limit(page.getLimit())
                        .totalPages(activityPage.getTotalPages())
                        .totalElements(activityPage.getTotalElements())
                        .build())
                .filter(filter)
                .build();
    }

    private Specification<Activity> filterActivities(ZoneId zoneId, ActivityFilter filter) {
        Specification<Activity> spec = Specification.where(ActivitySpecs.hasUser(filter.getUserId()));
        if (filter.getSearch() != null) {
            spec = spec.and(ActivitySpecs.hasName(filter.getSearch()));
        }
        if (filter.getSportIds() != null) {
            spec = spec.and(ActivitySpecs.hasSportIds(filter.getSportIds()));
        }
        if (filter.getDistance() != null) {
            spec = spec.and(ActivitySpecs.hasTotalDistanceBetween(
                    filter.getDistance().getMin(),
                    filter.getDistance().getMax())
            );
        }
        if (filter.getElevation() != null) {
            spec = spec.and(ActivitySpecs.hasElevationGainBetween(
                    filter.getElevation().getMin(),
                    filter.getElevation().getMax())
            );
        }
        if (filter.getTime() != null) {
            spec = spec.and(ActivitySpecs.hasMovingTimeSecondsBetween(
                    filter.getTime().getMin(),
                    filter.getTime().getMax())
            );
        }
        if (filter.getDate() != null) {
            Instant min = DateUtils.toStartOfDayInstant(
                    DateUtils.toInstant(filter.getDate().getMin()),
                    zoneId
            );
            Instant max = DateUtils.toEndOfDayInstant(
                    DateUtils.toInstant(filter.getDate().getMax()),
                    zoneId
            );

            spec = spec.and(ActivitySpecs.hasCreatedAtBetween(min, max));
        }
        return spec;
    }

    @Override
    @Transactional
    public ActivityResponse getActivity(String activityId) {
        ProfileResponse user = profileService.viewProfile();
        Activity activity = Common.findActivityById(activityId, activityRepository);

        return activityMapper.mapToActivityResponse(
                activity,
                user
        );
    }

    private CompletableFuture<Void> runAsyncSecurityContextWithSpan(String spanName, Span parent, Runnable task) {
        return TaskHelper.runAsyncSecurityContextWithSpan(
                spanName,
                parent,
                task,
                asyncExecutor,
                tracer
        );
    }

    private void trace(String name, Runnable runnable) {
        Span span = tracer.nextSpan().name(name).start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            runnable.run();
        } finally {
            span.end();
        }
    }

    @Override
    @Transactional
    public ActivityShortResponse createActivity(ZoneId zoneId, CreateActivityRequest request) {
        ProfileResponse user = profileService.viewProfile();
        Sport sport = Common.findReadOnlySportById(request.getSportId(), sportRepository, entityManager);

        Activity activity = prepareActivity(request, user, sport);
        Activity savedActivity = activityRepository.save(activity);

        if (sport.getSportMapType() == SportMapType.NO_MAP) {
            processActivityDoNotHaveMap(zoneId, request, savedActivity, sport, user);
        } else {
            processActivityHasMap(zoneId, request, savedActivity, user);
        }

        addProgress(savedActivity);

        Activity finalActivity = activityRepository.save(savedActivity);

        return activityMapper.mapToShortResponse(
                finalActivity,
                user
        );
    }

    private void processActivityDoNotHaveMap(
            ZoneId zoneId,
            CreateActivityRequest request,
            Activity activity,
            Sport sport,
            ProfileResponse user
    ) {
        activity.setLocation(null);

        activity.setRouteId(null);

        activity.setCarbonSaved(0.0);

        activity.setMapImage(null);

        activity.setElevations(new ArrayList<>());
        activity.setElevationGain(0);
        activity.setMaxElevation(0);

        activity.setSpeeds(new ArrayList<>());
        activity.setAvgSpeed(0.0);
        activity.setMaxSpeed(0.0);

        activity.setDistances(new ArrayList<>());
        activity.setTotalDistance(0.0);

        activity.setGeometry(null);

        activity.setCoordinatesTimestamps(new ArrayList<>());

        addCaloriesInfo(
                activity,
                request.getMovingTimeSeconds(),
                0.0,
                sport,
                user
        );

        addHeartRateInfo(activity, request, user);

        addGoalHistories(activity, zoneId);
    }

    private void processActivityHasMap(
            ZoneId zoneId,
            CreateActivityRequest request,
            Activity activity,
            ProfileResponse user
    ){
        mergeStartEndPoint(request);

        List<CoordinateRequest> sampled = ListUtils.minimized(request.getCoordinates(), NUMBER_CHART_POINTS);
        List<List<Double>> minimizedCoordinates = sampled.stream()
                .map(CoordinateRequest::getCoordinate)
                .toList();
        List<Long> minimizedTimestamps = new ArrayList<>(sampled.stream()
                .map(CoordinateRequest::getTimestamp)
                .toList());

        addSpeedAndDistanceInfo(activity, minimizedCoordinates, minimizedTimestamps);
        addGeometry(activity, request.getCoordinates());

        Span parent = tracer.currentSpan();

        CompletableFuture<Void> processLocationRouteFuture = runAsyncSecurityContextWithSpan(
                "process-location-route",
                parent,
                () -> processLocationRoute(activity, minimizedCoordinates)
        );

        CompletableFuture<Void> processOtherInfo = runAsyncSecurityContextWithSpan(
                "process-other-info",
                parent,
                () -> processOtherInfo(
                        activity,
                        request,
                        user,
                        minimizedCoordinates,
                        zoneId
                )
        );

        CompletableFuture.allOf(
                processLocationRouteFuture,
                processOtherInfo
        ).join();
    }

    private void mergeStartEndPoint(CreateActivityRequest request) {
        List<Double> start = request.getCoordinates()
                .get(0)
                .getCoordinate();
        List<Double> end = request.getCoordinates()
                .get(request.getCoordinates().size() - 1)
                .getCoordinate();

        double distance = GeometryUtils.distanceToPoint(start.get(1), start.get(0), end.get(1), end.get(0));

        if (distance <= THRESHOLD_METERS) {
            request.getCoordinates()
                    .get(request.getCoordinates().size() - 1)
                    .setCoordinate(List.of(start.get(0), start.get(1)));
        }
    }

    private Activity prepareActivity(
            CreateActivityRequest request,
            ProfileResponse user,
            Sport sport
    ) {
        return Activity.builder()
                .userId(user.getId())
                .routeId(request.getRouteId())
                .name(request.getName())
                .description(request.getDescription())
                .sport(sport)
                .sportId(sport.getId())
                .movingTimeSeconds(request.getMovingTimeSeconds())
                .elapsedTimeSeconds(request.getElapsedTimeSeconds())
                .rpe(request.getRpe())
                .images(request.getImages())
                .build();
    }

    private void addSpeedAndDistanceInfo(
            Activity activity,
            List<List<Double>> minimizedCoordinates,
            List<Long> minimizedTimestamps
    ) {
        SpeedCalculatorResult speedResult = speedCalculator.calculate(minimizedCoordinates, minimizedTimestamps);

        double totalDistance = speedResult.distances().get(speedResult.distances().size() - 1);
        activity.setDistances(speedResult.distances());
        activity.setTotalDistance(
                NumberUtils.round(
                        totalDistance,
                        RoundRules.DISTANCE.getValue()
                )
        );

        activity.setSpeeds(speedResult.speeds());
        activity.setMaxSpeed(speedResult.maxSpeed());
        activity.setAvgSpeed(speedResult.avgSpeed());

        activity.setCoordinatesTimestamps(minimizedTimestamps);
    }

    private void addLocation(Activity activity, List<List<Double>> coordinates) {
        GetLocationByGeometryResponse locationResponse = supabaseService.getLocationByGeometry(
                GetLocationByGeometryRequest.builder()
                        .geometry(GeometryRequest.builder()
                                .type(GeometryType.LINESTRING.getValue())
                                .coordinates(coordinates)
                                .build())
                        .build()
        );

        activity.setLocation(Location.builder()
                .district(locationResponse.getDistrict())
                .city(locationResponse.getCity())
                .ward(locationResponse.getWard())
                .build());
    }

    private void addGeometry(Activity activity, List<CoordinateRequest> rawCoordinates) {
        List<List<Double>> coordinates = extractCoordinates(rawCoordinates);

        String encodedCoordinate = StridePolylineUtils.encode(coordinates);
        activity.setGeometry(encodedCoordinate);
    }

    private List<List<Double>> extractCoordinates(List<CoordinateRequest> rawCoordinates) {
        return rawCoordinates.stream()
                .map(CoordinateRequest::getCoordinate)
                .toList();
    }

    private void processLocationRoute(
            Activity activity,
            List<List<Double>> minimizedCoordinates
    ) {
        trace("add-location", () -> addLocation(
                activity,
                minimizedCoordinates
        ));

        trace("add-route", ()->processRoute(activity));
    }

    private void processRoute(Activity activity) {
        if (activity.getRouteId() != null) {
            routeServiceImpl.updateRoute(
                    activity.getRouteId(),
                    UpdateRouteRequest.builder()
                            .activityId(activity.getId())
                            .images(activity.getImages())
                            .avgTime(Double.valueOf(activity.getMovingTimeSeconds()))
                            .avgDistance(activity.getTotalDistance())
                            .build()
            );
        } else {
            CreateRouteResponse routeResponse = routeServiceImpl.createRoute(
                    CreateRouteRequest.builder()
                            .sportId(activity.getSport().getId())
                            .activityId(activity.getId())
                            .sportMapType(activity.getSport().getSportMapType())
                            .images(activity.getImages())
                            .avgTime(activity.getMovingTimeSeconds().doubleValue())
                            .avgDistance(activity.getTotalDistance())
                            .geometry(activity.getGeometry())
                            .ward(activity.getLocation().getWard())
                            .district(activity.getLocation().getDistrict())
                            .city(activity.getLocation().getCity())
                            .build()
            );
            activity.setRouteId(routeResponse.getRouteId());
        }
    }

    private void processOtherInfo(
            Activity activity,
            CreateActivityRequest request,
            ProfileResponse user,
            List<List<Double>> minimizedCoordinates,
            ZoneId zoneId
    ) {
        trace("add-calories-info", () -> addCaloriesInfo(
                activity,
                request.getMovingTimeSeconds(),
                activity.getAvgSpeed(),
                activity.getSport(),
                user
        ));

        trace("add-elevation-info", () -> addElevationInfo(activity, minimizedCoordinates));

        trace("add-heart-rate-info", () -> addHeartRateInfo(activity, request, user));

        trace("add-carbon-saved-info", () ->
                addCarbonSavedInfo(activity, activity.getTotalDistance())
        );

        trace("add-map-image", () ->
                addMapImage(activity, request.getCoordinates())
        );

        trace("add-goal-histories", () -> addGoalHistories(activity, zoneId));
    }

    private void addHeartRateInfo(
            Activity activity,
            CreateActivityRequest request,
            ProfileResponse user) {
        if (request.getHeartRates() == null) {
            request.setHeartRates(new ArrayList<>());
        }

        List<Integer> sampledHeartRate = ListUtils.minimized(
                request.getHeartRates(),
                NUMBER_CHART_POINTS
        );

        if (user.getHeartRateZones() == null) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.USER_HAVE_NOT_HEART_RATE_ZONES_YET);
        }

        HeartRateCalculatorResult result = heartRateCalculator.calculate(
                sampledHeartRate,
                user.getHeartRateZones()
        );
        activity.setHeartRateZones(result.heartRateZones());
        activity.setAvgHearRate(result.avgHeartRate());
        activity.setMaxHearRate(result.maxHeartRate());
        activity.setHeartRates(request.getHeartRates());
    }

    private void addElevationInfo(Activity activity, List<List<Double>> coordinates) {
        List<Integer> elevations = elevationService.calculateElevations(coordinates);

        ElevationCalculatorResult elevationResult = elevationCalculator.calculate(elevations);
        activity.setElevations(elevationResult.elevations());
        activity.setElevationGain(elevationResult.elevationGain());
        activity.setMaxElevation(elevationResult.maxElevation());
    }

    private void addGoalHistories(Activity activity, ZoneId zoneId) {
        List<Goal> goals = goalRepository.findBySportId(activity.getSport().getId());

        List<GoalHistory> histories = new ArrayList<>();
        for (Goal goal : goals) {
            Long amount = 0L;
            if (goal.isActive()) {
                amount = getAmountGoal(goal.getType(), activity);
            }

            Calendar calendar = GoalTimeFrameHelper.getCalendar(goal.getTimeFrame(), zoneId);
            Date goalDate = calendar.getTime();

            String historyKey = GoalTimeFrameHelper.formatDateKey(goalDate, goal.getTimeFrame());
            Optional<GoalHistory> historyOptional = goalHistoryRepository.findByGoalIdAndDateKey(goal.getId(), historyKey);

            GoalHistory history;
            if (historyOptional.isPresent()) {
                history = historyOptional.get();
                history.setAmountGain(history.getAmountGain() + amount);
            } else {
                history = GoalHistory.builder()
                        .goal(goal)
                        .dateKey(historyKey)
                        .amountGoal(goal.getAmount())
                        .amountGain(amount)
                        .date(goalDate)
                        .build();
            }

            histories.add(history);
        }
        goalHistoryRepository.saveAll(histories);

        activity.setGoalHistories(histories);
    }

    private Long getAmountGoal(GoalType type, Activity activity) {
        Long amount = 0L;
        if (type.equals(GoalType.ACTIVITY)) {
            amount = 1L;
        } else if (type.equals(GoalType.ELEVATION)) {
            amount = activity.getElevationGain().longValue();
        } else if (type.equals(GoalType.DISTANCE)) {
            amount = activity.getTotalDistance().longValue();
        } else if (type.equals(GoalType.TIME)) {
            amount = activity.getMovingTimeSeconds();
        }
        return amount;
    }

    private void addCarbonSavedInfo(Activity activity, double distance) {
        double carbonSaved = carbonSavedCalculator.calculate(distance);
        activity.setCarbonSaved(carbonSaved);
    }

    private void addMapImage(Activity activity, List<CoordinateRequest> rawCoordinates) {
        List<List<Double>> coordinates = extractCoordinates(rawCoordinates);

        List<List<Double>> smoothCoordinates = RamerDouglasPeucker.handle(
                coordinates,
                RDP_EPSILON
        );

        String encodePolyline = StridePolylineUtils.encode(smoothCoordinates);

        String mapImage = mapboxService.generateAndUpload(encodePolyline, "activity");

        activity.setMapImage(mapImage);
    }

    private void addCaloriesInfo(
            Activity activity,
            long movingTimeSeconds,
            double avgSpeed,
            Sport sport,
            ProfileResponse user
    ) {
        int calories = calculateCalories(movingTimeSeconds, avgSpeed, sport, user);
        activity.setCalories(calories);
    }

    private int calculateCalories(
            long movingTimeSeconds,
            double avgSpeed,
            Sport sport,
            ProfileResponse user
    ) {
        double equipmentWeight = Optional.ofNullable(user.getEquipmentsWeight())
                .map(m ->
                        m.values().stream()
                                .mapToInt(Integer::intValue)
                                .sum()
                )
                .orElse(0);
        int weight = Optional.ofNullable(user.getWeight()).orElse(0);

        return caloriesCalculator.calculateCalories(
                sport,
                weight,
                movingTimeSeconds,
                Map.of(
                        RuleCaloriesType.SPEED, avgSpeed,
                        RuleCaloriesType.EQUIPMENT_WEIGHT, equipmentWeight,
                        RuleCaloriesType.TIME, (double) movingTimeSeconds
                )
        );
    }

    private void addProgress(Activity activity) {
        kafkaProducer.send(
                KafkaTopics.ACTIVITY_CREATED_TOPIC,
                ActivityCreatedEvent.builder()
                        .activityId(activity.getId())
                        .name(activity.getName())
                        .userId(activity.getUserId())
                        .sportId(activity.getSport().getId())
                        .mapImage(activity.getMapImage())
                        .distance((long) (activity.getTotalDistance() * 1000))
                        .movingTimeSeconds(activity.getMovingTimeSeconds())
                        .elevationGain(Long.valueOf(activity.getElevationGain()))
                        .avgHearRate(activity.getAvgHearRate())
                        .calories(activity.getCalories())
                        .time(activity.getCreatedAt())
                        .build()
        );
    }

    @Override
    @Transactional
    public void updateActivity(String activityId, UpdateActivityRequest request) {
        Activity activity = Common.findActivityById(activityId, activityRepository);

        if (request.getSportId() != null) {
            ProfileResponse user = profileService.viewProfile();

            Sport sport = Common.findReadOnlySportById(request.getSportId(), sportRepository, entityManager);

            addCaloriesInfo(
                    activity,
                    activity.getMovingTimeSeconds(),
                    activity.getAvgSpeed(),
                    sport,
                    user
            );
        }

        if (request.getName() != null) {
            activity.setName(request.getName());
            sendActivityUpdatedMetric(activity);
        }
        UpdateHelper.updateIfNotNull(request.getName(), activity::setName);
        UpdateHelper.updateIfNotNull(request.getDescription(), activity::setDescription);

        UpdateHelper.updateIfNotNull(request.getRpe(), activity::setRpe);

        if (request.getImages() != null) {
            UpdateHelper.updateIfNotNull(request.getImages(), activity::setImages);
            if (activity.getRouteId() != null) {
                routeServiceImpl.updateRoute(
                        activity.getRouteId(),
                        UpdateRouteRequest.builder()
                                .activityId(activity.getId())
                                .images(activity.getImages())
                                .build()
                );
            }
        }

        activityRepository.save(activity);
    }

    private void sendActivityUpdatedMetric(Activity activity) {
        kafkaProducer.send(
                KafkaTopics.ACTIVITY_UPDATED_TOPIC,
                ActivityUpdatedEvent.builder()
                        .activityId(activity.getId())
                        .name(activity.getName())
                        .build()
        );
    }

    @Override
    @Transactional
    public void deleteActivity(String activityId) {
        Activity activity = Common.findActivityById(activityId, activityRepository);

        String currentUserId = SecurityUtils.getCurrentUserId();
        if (!activity.getUserId().equals(currentUserId)) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.CAN_NOT_DELETE_OTHER_USER_ACTIVITIES);
        }

        deleteGoalHistory(activity);

        deleteProgress(activity);

        activityRepository.delete(activity);
    }

    private void deleteGoalHistory(Activity activity) {
        List<GoalHistory> histories = activity.getGoalHistories();
        for (GoalHistory history : histories) {
            Long amount = getAmountGoal(history.getGoal().getType(), activity);
            history.setAmountGain(history.getAmountGain() - amount);
        }
        goalHistoryRepository.saveAll(histories);
    }

    private void deleteProgress(Activity activity) {
        kafkaProducer.send(
                KafkaTopics.ACTIVITY_DELETED_TOPIC,
                ActivityDeletedEvent.builder()
                        .activityId(activity.getId())
                        .build()
        );
    }
}
