package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.commons.dto.page.AppPageResponse;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.commons.utils.UpdateHelper;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.coreservice.constant.RuleCaloriesType;
import com.stride.tracking.coreservice.mapper.ActivityMapper;
import com.stride.tracking.coreservice.mapper.CategoryMapper;
import com.stride.tracking.coreservice.mapper.SportMapper;
import com.stride.tracking.coreservice.model.Activity;
import com.stride.tracking.coreservice.model.Sport;
import com.stride.tracking.coreservice.utils.RouteUtils;
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
import com.stride.tracking.coreservice.utils.StridePolylineUtils;
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
import com.stride.tracking.dto.user.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityServiceImpl implements ActivityService {
    private final ActivityRepository activityRepository;
    private final SportRepository sportRepository;

    private final MapboxService mapboxService;
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

    @Value("${app.rdp.epsilon}")
    private double rdpEpsilon;

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

        Activity.ActivityBuilder builder = Activity.builder()
                .userId(user.getId())
                .routeId(request.getRouteId())
                .name(request.getName())
                .description(request.getDescription())
                .sport(sport)
                .totalDistance(request.getTotalDistance())
                .movingTimeSeconds(request.getMovingTimeSeconds())
                .elapsedTimeSeconds(request.getElapsedTimeSeconds())
                .avgSpeed(request.getAvgSpeed())
                .rpe(request.getRpe())
                .heartRates(request.getHeartRates())
                .images(request.getImages());

        List<List<Double>> coordinates = request.getCoordinates()
                .stream()
                .map(CoordinateRequest::getCoordinate).toList();
        coordinates = RouteUtils.mergeCloseStartEnd(coordinates, 5);

        List<Long> coordinatesTimestamps = new ArrayList<>(request.getCoordinates()
                .stream()
                .map(CoordinateRequest::getTimestamp).toList());
        coordinatesTimestamps.add(coordinatesTimestamps.get(coordinates.size() - 1));

        List<Integer> elevations = elevationService.calculateElevations(coordinates);

        addCaloriesInfo(
                builder,
                request.getMovingTimeSeconds(),
                request.getAvgSpeed(),
                sport,
                user
        );
        addSpeedInfo(builder, coordinates, coordinatesTimestamps);
        addElevationInfo(builder, elevations);
        addCarbonSavedInfo(builder, request.getTotalDistance());
        addHeartRateInfo(builder, request, user);
        addMapImage(builder, coordinates);

        Activity activity = builder.build();

        activity = activityRepository.save(activity);

        if (activity.getRouteId() != null) {
            routeService.updateRoute(
                    activity.getRouteId(),
                    UpdateRouteRequest.builder()
                            .activityId(activity.getId())
                            .images(activity.getImages())
                            .avgTime(activity.getMovingTimeSeconds())
                            .build()
            );
        }

        return activityMapper.mapToShortResponse(
                activity,
                sportMapper.mapToResponse(sport, categoryMapper.mapToCategoryResponse(sport.getCategory())),
                activityMapper.mapToUserResponse(user)
        );
    }


    private void addCaloriesInfo(Activity.ActivityBuilder builder, long movingTimeSeconds, double avgSpeed, Sport sport, UserResponse user) {
        int calories = calculateCalories(movingTimeSeconds, avgSpeed, sport, user);
        builder.calories(calories);
    }

    private int calculateCalories(long movingTimeSeconds, double avgSpeed, Sport sport, UserResponse user) {
        double equipmentWeight = user.getEquipmentsWeight().values().stream()
                .mapToInt(Integer::intValue)
                .sum();

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

    private void addSpeedInfo(Activity.ActivityBuilder builder, List<List<Double>> coordinates, List<Long> timestamps) {
        SpeedCalculatorResult speedResult = speedCalculator.calculate(coordinates, timestamps);
        builder.speeds(speedResult.speeds());
        builder.maxSpeed(speedResult.maxSpeed());
        builder.coordinates(coordinates);
        builder.coordinatesTimestamps(timestamps);
    }

    private void addElevationInfo(Activity.ActivityBuilder builder, List<Integer> elevations) {
        ElevationCalculatorResult elevationResult = elevationCalculator.calculate(elevations);
        builder.elevations(elevationResult.elevations());
        builder.elevationGain(elevationResult.elevationGain());
        builder.maxElevation(elevationResult.maxElevation());
    }

    private void addCarbonSavedInfo(Activity.ActivityBuilder builder, double distance) {
        double carbonSaved = carbonSavedCalculator.calculate(distance);
        builder.carbonSaved(carbonSaved);
    }

    private void addHeartRateInfo(Activity.ActivityBuilder builder, CreateActivityRequest request, UserResponse user) {
        HeartRateCalculatorResult result = heartRateCalculator.calculate(request.getHeartRates(), user.getHeartRateZones());
        builder.heartRateZones(result.heartRateZones());
        builder.avgHearRate(result.avgHeartRate());
        builder.maxHearRate(result.maxHeartRate());
    }

    private void addMapImage(Activity.ActivityBuilder builder, List<List<Double>> coordinates) {
        List<List<Double>> smoothCoordinates = RamerDouglasPeucker.douglasPeucker(coordinates, rdpEpsilon);

        String encodePolyline = StridePolylineUtils.encode(smoothCoordinates);
        String mapImage = mapboxService.generateAndUpload(encodePolyline, "activity");

        builder.mapImage(mapImage);
        builder.smoothCoordinates(smoothCoordinates);
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

    private void addCaloriesInfo(Activity activity, long movingTimeSeconds, double avgSpeed, Sport sport, UserResponse user) {
        int calories = calculateCalories(movingTimeSeconds, avgSpeed, sport, user);
        activity.setCalories(calories);
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

        CreateRouteResponse routeResponse = routeService.createRoute(
                CreateRouteRequest.builder()
                        .sportId(activity.getSport().getId())
                        .activityId(activityId)
                        .sportMapType(activity.getSport().getSportMapType().getLowercase())
                        .images(activity.getImages())
                        .avgTime(activity.getMovingTimeSeconds().doubleValue())
                        .coordinates(activity.getCoordinates())
                        .build()
        );

        activity.setRouteId(routeResponse.getRouteId());

        activityRepository.save(activity);
    }
}
