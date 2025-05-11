package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.commons.dto.page.AppPageResponse;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.commons.utils.UpdateHelper;
import com.stride.tracking.coreservice.constant.GeometryType;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.coreservice.constant.RoundRules;
import com.stride.tracking.coreservice.constant.RuleCaloriesType;
import com.stride.tracking.coreservice.mapper.ActivityMapper;
import com.stride.tracking.coreservice.mapper.CategoryMapper;
import com.stride.tracking.coreservice.mapper.SportMapper;
import com.stride.tracking.coreservice.model.Activity;
import com.stride.tracking.coreservice.model.Location;
import com.stride.tracking.coreservice.model.Sport;
import com.stride.tracking.coreservice.utils.*;
import com.stride.tracking.dto.activity.request.ActivityFilter;
import com.stride.tracking.dto.activity.request.CoordinateRequest;
import com.stride.tracking.dto.activity.request.CreateActivityRequest;
import com.stride.tracking.dto.activity.request.UpdateActivityRequest;
import com.stride.tracking.dto.activity.response.ActivityResponse;
import com.stride.tracking.dto.activity.response.ActivityShortResponse;
import com.stride.tracking.dto.activity.response.ActivityUserResponse;
import com.stride.tracking.coreservice.repository.ActivityRepository;
import com.stride.tracking.coreservice.repository.SportRepository;
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
import com.stride.tracking.dto.route.request.CreateRouteRequest;
import com.stride.tracking.dto.route.request.UpdateRouteRequest;
import com.stride.tracking.dto.route.response.CreateRouteResponse;
import com.stride.tracking.dto.supabase.request.GeometryRequest;
import com.stride.tracking.dto.supabase.request.GetLocationByGeometryRequest;
import com.stride.tracking.dto.supabase.response.GetLocationByGeometryResponse;
import com.stride.tracking.dto.user.response.UserResponse;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityServiceImpl implements ActivityService {
    private final ActivityRepository activityRepository;
    private final SportRepository sportRepository;

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
    private final SportMapper sportMapper;
    private final CategoryMapper categoryMapper;

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

        ActivityUserResponse activityUserResponse = activityMapper.mapToUserResponse(userResponse);
        List<ActivityShortResponse> data = activities.stream().map(activity -> activityMapper.mapToShortResponse(
                activity,
                sportMapper.mapToResponse(activity.getSport(), categoryMapper.mapToCategoryResponse(activity.getSport().getCategory())),
                activityUserResponse
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
                sportMapper.mapToResponse(activity.getSport(), categoryMapper.mapToCategoryResponse(activity.getSport().getCategory())),
                activityMapper.mapToUserResponse(user)
        );
    }

    @Override
    @Transactional
    public ActivityShortResponse createActivity(CreateActivityRequest request) {
        Sport sport = Common.findSportById(request.getSportId(), sportRepository);
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

        processCoordinatesData(activity, request);

        addCaloriesInfo(
                activity,
                request.getMovingTimeSeconds(),
                activity.getAvgSpeed(),
                sport,
                user
        );
        addHeartRateInfo(activity, request, user);

        activity = activityRepository.save(activity);

        updateRoute(activity);

        return activityMapper.mapToShortResponse(
                activity,
                sportMapper.mapToResponse(sport, categoryMapper.mapToCategoryResponse(sport.getCategory())),
                activityMapper.mapToUserResponse(user)
        );
    }

    public void mergeStartEndPoint(CreateActivityRequest request) {
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
                    .setCoordinate(new double[] {start[0], start[1]});
        }
    }

    private void processCoordinatesData(Activity activity, CreateActivityRequest request) {
        if (activity.getSport().getSportMapType() != null) {
            List<CoordinateRequest> rawCoordinates = request.getCoordinates();

            addGeometryAndMap(activity, rawCoordinates);
            processChartInfo(activity, rawCoordinates, request.getMovingTimeSeconds());

            //Ensure to add totalDistance before calling addCarbonSavedInfo
            addCarbonSavedInfo(activity, activity.getTotalDistance());
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

    private void processChartInfo(Activity activity, List<CoordinateRequest> rawCoordinates, Long movingTimeSeconds) {
        List<CoordinateRequest> sampled = ListUtils.minimized(rawCoordinates, NUMBER_CHART_POINTS);

        List<double[]> minimizedCoordinates = sampled.stream()
                .map(CoordinateRequest::getCoordinate)
                .toList();

        List<Long> minimizedTimestamps = new ArrayList<>(sampled.stream()
                .map(CoordinateRequest::getTimestamp)
                .toList());

        addSpeedInfo(
                activity,
                minimizedCoordinates,
                minimizedTimestamps,
                movingTimeSeconds
        );
        addElevationInfo(activity, minimizedCoordinates);
        addLocation(activity, minimizedCoordinates);
    }

    private void addSpeedInfo(
            Activity activity,
            List<double[]> coordinates,
            List<Long> timestamps,
            Long movingTimeSeconds
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

        activity.setSpeeds(speedResult.speeds());
        activity.setMaxSpeed(speedResult.maxSpeed());
        activity.setAvgSpeed(
                NumberUtils.round(
                        totalDistance / movingTimeSeconds,
                        RoundRules.SPEED.getValue()
                )
        );

        activity.setCoordinatesTimestamps(timestamps);

        String encodedCoordinate = StridePolylineUtils.encode(coordinates);
        activity.setGeometry(encodedCoordinate);
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

        return caloriesCalculator.calculateCalories(
                sport,
                user.getWeight(),
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

    private void updateRoute(Activity activity) {
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
        }
    }

    @Override
    @Transactional
    public void updateActivity(String activityId, UpdateActivityRequest request) {
        Activity activity = Common.findActivityById(activityId, activityRepository);

        if (request.getSportId() != null) {
            UserResponse user = profileService.viewProfile();

            Sport sport = Common.findSportById(request.getSportId(), sportRepository);

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

        activityRepository.delete(activity);
    }

    @Override
    @Transactional
    public void saveRoute(String activityId) {
        Activity activity = Common.findActivityById(activityId, activityRepository);

        if (activity.getRouteId() != null) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.CAN_NOT_CREATE_SAVED_ROUTE);
        }

        CreateRouteResponse routeResponse = routeService.createRoute(
                CreateRouteRequest.builder()
                        .sportId(activity.getSport().getId())
                        .activityId(activityId)
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

        activityRepository.save(activity);
    }
}
