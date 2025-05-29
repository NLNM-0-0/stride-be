package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.commons.dto.page.AppPageResponse;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.commons.utils.TaskHelper;
import com.stride.tracking.commons.utils.UpdateHelper;
import com.stride.tracking.coreservice.constant.GeometryType;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.coreservice.constant.RoundRules;
import com.stride.tracking.coreservice.constant.RuleCaloriesType;
import com.stride.tracking.coreservice.mapper.ActivityMapper;
import com.stride.tracking.coreservice.model.*;
import com.stride.tracking.coreservice.repository.GoalHistoryRepository;
import com.stride.tracking.coreservice.repository.GoalRepository;
import com.stride.tracking.coreservice.repository.ProgressRepository;
import com.stride.tracking.coreservice.utils.GoalTimeFrameHelper;
import com.stride.tracking.coreservice.utils.NumberUtils;
import com.stride.tracking.coreservice.model.Activity;
import com.stride.tracking.coreservice.model.Location;
import com.stride.tracking.coreservice.model.Sport;
import com.stride.tracking.coreservice.utils.*;
import com.stride.tracking.dto.activity.request.*;
import com.stride.tracking.dto.activity.response.ActivityResponse;
import com.stride.tracking.dto.activity.response.ActivityShortResponse;
import com.stride.tracking.coreservice.repository.ActivityRepository;
import com.stride.tracking.coreservice.repository.specs.ActivitySpecs;
import com.stride.tracking.coreservice.service.ActivityService;
import com.stride.tracking.coreservice.utils.calculator.CaloriesCalculator;
import com.stride.tracking.coreservice.utils.calculator.CarbonSavedCalculator;
import com.stride.tracking.coreservice.utils.calculator.RamerDouglasPeucker;
import com.stride.tracking.coreservice.utils.calculator.elevation.ElevationCalculator;
import com.stride.tracking.coreservice.utils.calculator.elevation.ElevationCalculatorResult;
import com.stride.tracking.coreservice.utils.calculator.heartrate.HeartRateCalculator;
import com.stride.tracking.coreservice.utils.calculator.heartrate.HeartRateCalculatorResult;
import com.stride.tracking.coreservice.utils.calculator.speed.SpeedCalculator;
import com.stride.tracking.coreservice.utils.calculator.speed.SpeedCalculatorResult;
import com.stride.tracking.dto.goal.GoalType;
import com.stride.tracking.dto.route.request.CreateRouteRequest;
import com.stride.tracking.dto.route.request.UpdateRouteRequest;
import com.stride.tracking.dto.route.response.CreateRouteResponse;
import com.stride.tracking.dto.supabase.request.GeometryRequest;
import com.stride.tracking.dto.supabase.request.GetLocationByGeometryRequest;
import com.stride.tracking.dto.supabase.response.GetLocationByGeometryResponse;
import com.stride.tracking.dto.user.response.UserResponse;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
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
    private final ProgressRepository progressRepository;

    private final SportCacheService sportCacheService;

    private final MapboxService mapboxService;
    private final SupabaseService supabaseService;
    private final RouteService routeService;
    private final ProfileService profileService;
    private final ElevationService elevationService;

    private final ElevationCalculator elevationCalculator;
    private final HeartRateCalculator heartRateCalculator;
    private final SpeedCalculator speedCalculator;
    private final CarbonSavedCalculator carbonSavedCalculator;
    private final CaloriesCalculator caloriesCalculator;

    private final ActivityMapper activityMapper;

    private final Executor asyncExecutor;
    private final Tracer tracer;

    private static final double RDP_EPSILON = 0.00005;
    private static final int NUMBER_CHART_POINTS = 100;
    private static final int THRESHOLD_METERS = 10;

    @Override
    @Transactional
    public ListResponse<ActivityShortResponse, ActivityFilter> getActivitiesOfUser(
            AppPageRequest page) {
        UserResponse userResponse = profileService.viewProfile();
        ActivityFilter filter = ActivityFilter.builder()
                .userId(userResponse.getId())
                .build();

        Pageable pageable = PageRequest.of(
                page.getPage() - 1,
                page.getLimit(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Specification<Activity> spec = filterActivities(filter);

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

    private Specification<Activity> filterActivities(ActivityFilter filter) {
        Specification<Activity> spec = Specification.where(null);
        if (filter.getUserId() != null) {
            spec = spec.and(ActivitySpecs.hasUser(filter.getUserId()));
        }
        return spec;
    }

    @Override
    @Transactional
    public ActivityResponse getActivity(String activityId) {
        UserResponse user = profileService.viewProfile();
        Activity activity = Common.findActivityById(activityId, activityRepository);

        return activityMapper.mapToActivityResponse(
                activity,
                user
        );
    }

    @Override
    @Transactional
    public ActivityShortResponse createActivity(ZoneId zoneId, CreateActivityRequest request) {
        Sport sport = sportCacheService.findSportById(request.getSportId());
        UserResponse user = profileService.viewProfile();

        mergeStartEndPoint(request);

        Activity activity = Activity.builder()
                .userId(user.getId())
                .routeId(request.getRouteId())
                .name(request.getName())
                .description(request.getDescription())
                .sport(sport) //Ensure to add sport here to check in processCoordinatesData step
                .movingTimeSeconds(request.getMovingTimeSeconds())
                .elapsedTimeSeconds(request.getElapsedTimeSeconds())
                .rpe(request.getRpe())
                .images(request.getImages())
                .build();

        processCoordinatesData(
                activity,
                request,
                user
        );

        addHeartRateInfo(activity, request, user);

        Activity savedActivity = activityRepository.save(activity);

        Span parent = tracer.currentSpan();

        CompletableFuture<Void> processRouteFuture = runAsyncSecurityContextWithSpan(
                "process-route",
                parent,
                () -> processRoute(savedActivity)
        );

        CompletableFuture<Void> addGoalHistoriesFuture = runAsyncSecurityContextWithSpan(
                "add-goal-histories",
                parent,
                () -> addGoalHistories(savedActivity, zoneId)
        );

        addProgress(savedActivity);

        CompletableFuture.allOf(
                processRouteFuture,
                addGoalHistoriesFuture
        ).join();

        return activityMapper.mapToShortResponse(
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

    private void mergeStartEndPoint(CreateActivityRequest request) {
        double[] start = request.getCoordinates()
                .get(0)
                .getCoordinate();
        double[] end = request.getCoordinates()
                .get(request.getCoordinates().size() - 1)
                .getCoordinate();

        double distance = GeometryUtils.distanceToPoint(start[1], start[0], end[1], end[0]);

        if (distance <= THRESHOLD_METERS) {
            request.getCoordinates()
                    .get(request.getCoordinates().size() - 1)
                    .setCoordinate(new double[]{start[0], start[1]});
        }
    }

    private void processCoordinatesData(
            Activity activity,
            CreateActivityRequest request,
            UserResponse user
    ) {
        if (activity.getSport().getSportMapType() != null) {
            List<CoordinateRequest> rawCoordinates = request.getCoordinates();

            Span parent = tracer.currentSpan();

            CompletableFuture<Void> addGeometryMapFuture = runAsyncSecurityContextWithSpan(
                    "add-geometry_map",
                    parent,
                    () -> addGeometryAndMap(activity, rawCoordinates)
            );

            CompletableFuture<Void> processChartInfoFuture = runAsyncSecurityContextWithSpan(
                    "process-chart-info",
                    parent,
                    () -> processChartInfo(
                            activity,
                            rawCoordinates,
                            request.getMovingTimeSeconds(),
                            user
                    )
            );

            CompletableFuture.allOf(
                    addGeometryMapFuture,
                    processChartInfoFuture
            ).join();
        }
    }

    private void addGeometryAndMap(Activity activity, List<CoordinateRequest> rawCoordinates) {
        List<double[]> coordinates = rawCoordinates.stream()
                .map(CoordinateRequest::getCoordinate)
                .toList();

        addGeometry(activity, coordinates);
        addMapImage(activity, coordinates);
    }

    private void addGeometry(Activity activity, List<double[]> coordinates) {
        String encodedCoordinate = StridePolylineUtils.encode(coordinates);
        activity.setGeometry(encodedCoordinate);
    }

    private void addMapImage(Activity activity, List<double[]> coordinates) {
        List<double[]> smoothCoordinates = RamerDouglasPeucker.handle(
                coordinates,
                RDP_EPSILON
        );

        String encodePolyline = StridePolylineUtils.encode(smoothCoordinates);

        String mapImage = mapboxService.generateAndUpload(encodePolyline, "activity");

        activity.setMapImage(mapImage);
    }

    private void processChartInfo(
            Activity activity,
            List<CoordinateRequest> rawCoordinates,
            Long movingTimeSeconds,
            UserResponse user
    ) {
        List<CoordinateRequest> sampled = ListUtils.minimized(rawCoordinates, NUMBER_CHART_POINTS);

        List<double[]> minimizedCoordinates = sampled.stream()
                .map(CoordinateRequest::getCoordinate)
                .toList();

        List<Long> minimizedTimestamps = new ArrayList<>(sampled.stream()
                .map(CoordinateRequest::getTimestamp)
                .toList());

        Span parent = tracer.currentSpan();

        //Run add speed info, elevation, location in multiple task
        CompletableFuture<Void> addSpeedInfoFuture = runAsyncSecurityContextWithSpan(
                "add-speed-info",
                parent,
                () -> addSpeedInfo(
                        activity,
                        minimizedCoordinates,
                        minimizedTimestamps,
                        movingTimeSeconds,
                        user
                )
        );

        CompletableFuture<Void> addElevationInfoFuture = runAsyncSecurityContextWithSpan(
                "add-elevation-info",
                parent,
                () -> addElevationInfo(
                        activity,
                        minimizedCoordinates
                )
        );

        CompletableFuture<Void> addLocationFuture = runAsyncSecurityContextWithSpan(
                "add-location-info",
                parent,
                () -> addLocation(
                        activity,
                        minimizedCoordinates
                )
        );

        CompletableFuture.allOf(
                addSpeedInfoFuture,
                addElevationInfoFuture,
                addLocationFuture
        ).join();
    }

    private void addSpeedInfo(
            Activity activity,
            List<double[]> coordinates,
            List<Long> timestamps,
            Long movingTimeSeconds,
            UserResponse user
    ) {
        SpeedCalculatorResult speedResult = speedCalculator.calculate(coordinates, timestamps);

        double totalDistance = speedResult.distances().get(speedResult.distances().size() - 1);
        activity.setDistances(speedResult.distances());
        activity.setTotalDistance(
                NumberUtils.round(
                        totalDistance,
                        RoundRules.DISTANCE.getValue()
                )
        );

        //Ensure to add totalDistance before calling addCarbonSavedInfo
        addCarbonSavedInfo(activity, activity.getTotalDistance());

        activity.setSpeeds(speedResult.speeds());
        activity.setMaxSpeed(speedResult.maxSpeed());
        activity.setAvgSpeed(speedResult.avgSpeed());

        activity.setCoordinatesTimestamps(timestamps);

        addCaloriesInfo(
                activity,
                movingTimeSeconds,
                activity.getAvgSpeed(),
                activity.getSport(),
                user
        );
    }

    private void addCaloriesInfo(
            Activity activity,
            long movingTimeSeconds,
            double avgSpeed,
            Sport sport,
            UserResponse user
    ) {
        int calories = calculateCalories(movingTimeSeconds, avgSpeed, sport, user);
        activity.setCalories(calories);
    }

    private int calculateCalories(
            long movingTimeSeconds,
            double avgSpeed,
            Sport sport,
            UserResponse user
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
                        RuleCaloriesType.EQUIPMENT_WEIGHT, equipmentWeight
                )
        );
    }

    private void addElevationInfo(Activity activity, List<double[]> coordinates) {
        List<Integer> elevations = elevationService.calculateElevations(coordinates);

        ElevationCalculatorResult elevationResult = elevationCalculator.calculate(elevations);
        activity.setElevations(elevationResult.elevations());
        activity.setElevationGain(elevationResult.elevationGain());
        activity.setMaxElevation(elevationResult.maxElevation());
    }

    private void addLocation(Activity activity, List<double[]> coordinates) {
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

    private void addCarbonSavedInfo(Activity activity, double distance) {
        double carbonSaved = carbonSavedCalculator.calculate(distance);
        activity.setCarbonSaved(carbonSaved);
    }

    private void addHeartRateInfo(
            Activity activity,
            CreateActivityRequest request,
            UserResponse user) {
        List<Integer> sampledHeartRate = ListUtils.minimized(
                request.getHeartRates(),
                NUMBER_CHART_POINTS
        );

        HeartRateCalculatorResult result = heartRateCalculator.calculate(
                sampledHeartRate,
                user.getHeartRateZones()
        );
        activity.setHeartRateZones(result.heartRateZones());
        activity.setAvgHearRate(result.avgHeartRate());
        activity.setMaxHearRate(result.maxHeartRate());
        activity.setHeartRates(request.getHeartRates());
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

    private void addProgress(Activity activity) {
        Progress progress = Progress.builder()
                .userId(activity.getUserId())
                .activity(activity)
                .sport(activity.getSport())
                .activity(activity)
                .distance((long) (activity.getTotalDistance() * 1000))
                .time(activity.getMovingTimeSeconds())
                .elevation(activity.getElevationGain())
                .build();

        progressRepository.save(progress);
    }

    private void processRoute(Activity activity) {
        if (activity.getRouteId() != null) {
            routeService.updateRoute(
                    activity.getRouteId(),
                    UpdateRouteRequest.builder()
                            .activityId(activity.getId())
                            .images(activity.getImages())
                            .avgTime(activity.getMovingTimeSeconds())
                            .avgDistance(activity.getTotalDistance())
                            .build()
            );
        } else {
            CreateRouteResponse routeResponse = routeService.createRoute(
                    CreateRouteRequest.builder()
                            .sportId(activity.getSport().getId())
                            .activityId(activity.getId())
                            .sportMapType(activity.getSport().getSportMapType().name())
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

    @Override
    @Transactional
    public void updateActivity(String activityId, UpdateActivityRequest request) {
        Activity activity = Common.findActivityById(activityId, activityRepository);

        if (request.getSportId() != null) {
            UserResponse user = profileService.viewProfile();

            Sport sport = sportCacheService.findSportById(request.getSportId());

            addCaloriesInfo(
                    activity,
                    activity.getMovingTimeSeconds(),
                    activity.getAvgSpeed(),
                    sport,
                    user
            );
        }

        UpdateHelper.updateIfNotNull(request.getName(), activity::setName);
        UpdateHelper.updateIfNotNull(request.getDescription(), activity::setDescription);

        UpdateHelper.updateIfNotNull(request.getRpe(), activity::setRpe);

        if (request.getImages() != null) {
            UpdateHelper.updateIfNotNull(request.getImages(), activity::setImages);
            if (activity.getRouteId() != null) {
                routeService.updateRoute(
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
        progressRepository.deleteByActivity_Id(activity.getId());
    }
}
