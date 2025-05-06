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
import com.stride.tracking.coreservice.utils.GeometryUtils;
import com.stride.tracking.coreservice.utils.NumberUtils;
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

        Activity activity = Activity.builder()
                .userId(user.getId())
                .routeId(request.getRouteId())
                .name(request.getName())
                .description(request.getDescription())
                .sport(sport)
                .movingTimeSeconds(request.getMovingTimeSeconds())
                .elapsedTimeSeconds(request.getElapsedTimeSeconds())
                .rpe(request.getRpe())
                .images(request.getImages())
                .build();

        List<List<Double>> coordinates = request.getCoordinates()
                .stream()
                .map(CoordinateRequest::getCoordinate).toList();
        coordinates = RouteUtils.mergeCloseStartEnd(coordinates, 5);

        List<Long> coordinatesTimestamps = new ArrayList<>(request.getCoordinates()
                .stream()
                .map(CoordinateRequest::getTimestamp).toList());
        coordinatesTimestamps.add(coordinatesTimestamps.get(coordinates.size() - 1));

        List<Integer> elevations = elevationService.calculateElevations(coordinates);

        addSpeedInfo(
                activity,
                coordinates,
                coordinatesTimestamps,
                request.getMovingTimeSeconds()
        );
        addCaloriesInfo(
                activity,
                request.getMovingTimeSeconds(),
                activity.getAvgSpeed(),
                sport,
                user
        );
        addSpeedInfo(
                activity,
                coordinates,
                coordinatesTimestamps,
                request.getMovingTimeSeconds()
        );
        addElevationInfo(activity, elevations);
        addCarbonSavedInfo(activity, activity.getTotalDistance());
        addHeartRateInfo(activity, request, user);
        addMapImage(activity, coordinates);

        activity = activityRepository.save(activity);

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

        return activityMapper.mapToShortResponse(
                activity,
                sportMapper.mapToResponse(sport, categoryMapper.mapToCategoryResponse(sport.getCategory())),
                activityMapper.mapToUserResponse(user)
        );
    }

    private void addSpeedInfo(
            Activity activity,
            List<List<Double>> coordinates,
            List<Long> timestamps,
            Long movingTimeSeconds
    ) {
        SpeedCalculatorResult speedResult = speedCalculator.calculate(coordinates, timestamps);

        double totalDistance = speedResult.distances().get(speedResult.distances().size() - 1);
        activity.setDistances(speedResult.distances());
        activity.setTotalDistance(NumberUtils.round(totalDistance, 2));

        activity.setSpeeds(speedResult.speeds());
        activity.setMaxSpeed(speedResult.maxSpeed());
        activity.setAvgSpeed(NumberUtils.round(totalDistance / movingTimeSeconds, 5));

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

    private void addElevationInfo(Activity activity, List<Integer> elevations) {
        ElevationCalculatorResult elevationResult = elevationCalculator.calculate(elevations);
        activity.setElevations(elevationResult.elevations());
        activity.setElevationGain(elevationResult.elevationGain());
        activity.setMaxElevation(elevationResult.maxElevation());
    }

    private void addCarbonSavedInfo(Activity activity, double distance) {
        double carbonSaved = carbonSavedCalculator.calculate(distance);
        activity.setCarbonSaved(carbonSaved);
    }

    private void addHeartRateInfo(Activity activity, CreateActivityRequest request, UserResponse user) {
        HeartRateCalculatorResult result = heartRateCalculator.calculate(request.getHeartRates(), user.getHeartRateZones());
        activity.setHeartRateZones(result.heartRateZones());
        activity.setAvgHearRate(result.avgHeartRate());
        activity.setMaxHearRate(result.maxHeartRate());
        activity.setHeartRates(request.getHeartRates());
    }

    private void addMapImage(Activity activity, List<List<Double>> coordinates) {
        List<List<Double>> smoothCoordinates = RamerDouglasPeucker.douglasPeucker(coordinates, rdpEpsilon);

        String encodePolyline = StridePolylineUtils.encode(smoothCoordinates);
        String mapImage = mapboxService.generateAndUpload(encodePolyline, "activity");

        activity.setMapImage(mapImage);
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
                        .build()
        );

        activity.setRouteId(routeResponse.getRouteId());

        activityRepository.save(activity);
    }
}
