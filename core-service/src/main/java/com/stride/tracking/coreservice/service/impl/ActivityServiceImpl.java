package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.commons.dto.page.AppPageResponse;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.commons.utils.UpdateHelper;
import com.stride.tracking.coreservice.client.OpenElevationFeignClient;
import com.stride.tracking.coreservice.client.ProfileFeignClient;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.coreservice.constant.RuleCaloriesType;
import com.stride.tracking.coreservice.mapper.ActivityMapper;
import com.stride.tracking.coreservice.mapper.CategoryMapper;
import com.stride.tracking.coreservice.mapper.SportMapper;
import com.stride.tracking.coreservice.model.Activity;
import com.stride.tracking.coreservice.model.Sport;
import com.stride.tracking.coreservice.payload.activity.request.ActivityFilter;
import com.stride.tracking.coreservice.payload.activity.request.CoordinateRequest;
import com.stride.tracking.coreservice.payload.activity.request.CreateActivityRequest;
import com.stride.tracking.coreservice.payload.activity.request.UpdateActivityRequest;
import com.stride.tracking.coreservice.payload.activity.response.ActivityResponse;
import com.stride.tracking.coreservice.payload.activity.response.ActivityShortResponse;
import com.stride.tracking.coreservice.payload.activity.response.ActivityUserResponse;
import com.stride.tracking.coreservice.payload.elevation.request.ElevationRequest;
import com.stride.tracking.coreservice.payload.elevation.request.LocationRequest;
import com.stride.tracking.coreservice.payload.elevation.response.ElevationResponse;
import com.stride.tracking.coreservice.repository.ActivityRepository;
import com.stride.tracking.coreservice.repository.SportRepository;
import com.stride.tracking.coreservice.repository.specs.ActivitySpecs;
import com.stride.tracking.coreservice.service.ActivityService;
import com.stride.tracking.coreservice.utils.StridePolylineUtils;
import com.stride.tracking.coreservice.utils.calculator.CaloriesCalculator;
import com.stride.tracking.coreservice.utils.calculator.CarbonSavedCalculator;
import com.stride.tracking.coreservice.utils.calculator.elevation.ElevationCalculator;
import com.stride.tracking.coreservice.utils.calculator.elevation.ElevationCalculatorResult;
import com.stride.tracking.coreservice.utils.calculator.heartrate.HeartRateCalculator;
import com.stride.tracking.coreservice.utils.calculator.heartrate.HeartRateCalculatorResult;
import com.stride.tracking.coreservice.utils.calculator.speed.SpeedCalculator;
import com.stride.tracking.coreservice.utils.calculator.speed.SpeedCalculatorResult;
import com.stride.tracking.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityServiceImpl implements ActivityService {
    private final ActivityRepository activityRepository;
    private final SportRepository sportRepository;

    private final ProfileFeignClient profileClient;
    private final OpenElevationFeignClient elevationClient;

    private final MapboxService mapboxService;

    private final ElevationCalculator elevationCalculator;
    private final HeartRateCalculator heartRateCalculator;
    private final SpeedCalculator speedCalculator;
    private final CarbonSavedCalculator carbonSavedCalculator;
    private final CaloriesCalculator caloriesCalculator;

    private final ActivityMapper activityMapper;
    private final SportMapper sportMapper;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public ListResponse<ActivityShortResponse, ActivityFilter> getActivitiesOfUser(
            AppPageRequest page) {
        UserResponse userResponse = viewProfile();
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

    private UserResponse viewProfile() {
        ResponseEntity<UserResponse> response = profileClient.viewUser();
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.error("[viewProfile] Failed to view user profile for user id: {}", SecurityUtils.getCurrentUserId());
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.VIEW_PROFILE_FAILED);
        }

        log.debug("[viewProfile] Success to get user profile for user id: {}", response.getBody().getId());
        return Objects.requireNonNull(response.getBody());
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
        UserResponse user = viewProfile();
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
        UserResponse user = viewProfile();

        Activity.ActivityBuilder builder = Activity.builder()
                .userId(user.getId())
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
        List<Long> coordinatesTimestamps = request.getCoordinates()
                .stream()
                .map(CoordinateRequest::getTimestamp).toList();
        List<Integer> elevations = calculateElevations(coordinates);

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

        return activityMapper.mapToShortResponse(
                activity,
                sportMapper.mapToResponse(sport, categoryMapper.mapToCategoryResponse(sport.getCategory())),
                activityMapper.mapToUserResponse(user)
        );
    }

    private List<Integer> calculateElevations(List<List<Double>> coordinates) {
        List<LocationRequest> locationRequests = coordinates.stream()
                .map(coordinate -> {
                    Double longitude = coordinate.get(0);
                    Double latitude = coordinate.get(1);
                    return LocationRequest.builder()
                            .longitude(longitude)
                            .latitude(latitude)
                            .build();
                })
                .toList();

        ElevationRequest request = ElevationRequest.builder()
                .locations(locationRequests)
                .build();

        ResponseEntity<ElevationResponse> response = elevationClient.calculateElevation(request);

        if (!HttpStatus.OK.equals(response.getStatusCode()) || response.getBody() == null) {
            log.error("[getElevations] Failed to calculate elevations");
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.CALCULATE_ELEVATIONS_FAILED);
        }

        log.debug("[getElevations] Success to calculate elevations");

        return response.getBody().getResults().stream()
                .map(location -> location.getElevation().intValue())
                .toList();
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
        String encodePolyline = StridePolylineUtils.encode(coordinates);
        String mapImage = mapboxService.generateAndUpload(encodePolyline, "activity");

        builder.mapImage(mapImage);
    }

    @Override
    @Transactional
    public void updateActivity(String activityId, UpdateActivityRequest request) {
        Activity activity = Common.findActivityById(activityId, activityRepository);

        if (request.getSportId() != null) {
            UserResponse user = viewProfile();

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
        UpdateHelper.updateIfNotNull(request.getImages(), activity::setImages);
        UpdateHelper.updateIfNotNull(request.getRpe(), activity::setRpe);

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


}
