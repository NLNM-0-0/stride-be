package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.commons.dto.page.AppPageResponse;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.core.dto.mapbox.response.MapboxDirectionResponse;
import com.stride.tracking.core.dto.route.request.*;
import com.stride.tracking.core.dto.route.response.CreateRouteResponse;
import com.stride.tracking.core.dto.route.response.RouteResponse;
import com.stride.tracking.core.dto.route.response.SaveRouteResponse;
import com.stride.tracking.core.dto.sport.SportMapType;
import com.stride.tracking.core.dto.supabase.request.*;
import com.stride.tracking.core.dto.supabase.response.*;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.coreservice.mapper.RouteMapper;
import com.stride.tracking.coreservice.model.Location;
import com.stride.tracking.coreservice.model.Route;
import com.stride.tracking.coreservice.model.Sport;
import com.stride.tracking.coreservice.repository.RouteRepository;
import com.stride.tracking.coreservice.repository.SportRepository;
import com.stride.tracking.coreservice.repository.specs.RouteSpecs;
import com.stride.tracking.coreservice.service.RouteService;
import com.stride.tracking.coreservice.utils.ComposeNameHelper;
import com.stride.tracking.coreservice.utils.GeometryConverter;
import com.stride.tracking.coreservice.utils.StridePolylineUtils;
import com.stride.tracking.coreservice.utils.WayPointHelper;
import com.stride.tracking.coreservice.utils.calculator.RamerDouglasPeucker;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
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
@Log4j2
public class RouteServiceImpl implements RouteService {
    private final RouteRepository routeRepository;
    private final MapboxService mapboxService;
    private final SupabaseService supabaseService;

    private final RouteMapper routeMapper;
    private final SportRepository sportRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public SimpleListResponse<RouteResponse> getRecommendedRoutes(GetRecommendRouteRequest request) {
        RouteFilter filter = buildRouteFilter(request);

        Specification<Route> spec = filterRoutes(filter);

        List<Route> routes = routeRepository.findAll(spec);

        FindDistrictsNearPointResponse districts = supabaseService.findDistrictsNearPoint(
                FindDistrictsNearPointRequest.builder()
                        .lat(request.getLatitude())
                        .lon(request.getLongitude())
                        .around(request.getSportMapType().getRecommendedDistance())
                        .build()
        );
        List<String> districtNames = districts.getData().stream()
                .map(DistrictDistanceResponse::getDistrictName)
                .toList();

        List<RouteResponse> data = routes.stream()
                .filter(route -> route.getDistricts().stream().anyMatch(districtNames::contains))
                .limit(request.getLimit())
                .map(routeMapper::mapToRouteResponse)
                .toList();

        return new SimpleListResponse<>(data);
    }

    private RouteFilter buildRouteFilter(GetRecommendRouteRequest request) {
        return RouteFilter.builder()
                .sportId(request.getSportId())
                .publicRoute(true)
                .build();
    }

    private Specification<Route> filterRoutes(RouteFilter filter) {
        Specification<Route> spec = Specification.where(null);
        if (filter.getUserId() != null) {
            spec = spec.and(RouteSpecs.hasUserId(filter.getUserId()));
        }
        if (filter.getSportId() != null) {
            spec = spec.and(RouteSpecs.hasSportId(filter.getSportId()));
        }
        if (filter.getPublicRoute() != null) {
            spec = spec.and(RouteSpecs.isHaveUserId(!filter.getPublicRoute()));
        }

        return spec;
    }

    @Override
    @Transactional(readOnly = true)
    public ListResponse<RouteResponse, RouteFilter> getRoutes(AppPageRequest page, RouteFilter filter) {
        filter.setUserId(SecurityUtils.getCurrentUserId());

        Pageable pageable = PageRequest.of(
                page.getPage() - 1,
                page.getLimit(),
                Sort.by(Sort.Direction.ASC, "createdAt")
        );
        Specification<Route> spec = filterRoutes(filter);

        Page<Route> categoryPage = routeRepository.findAll(spec, pageable);

        List<Route> categories = categoryPage.getContent();

        List<RouteResponse> data = categories.stream().map(routeMapper::mapToRouteResponse).toList();

        return ListResponse.<RouteResponse, RouteFilter>builder()
                .data(data)
                .appPageResponse(AppPageResponse.builder()
                        .index(page.getPage())
                        .limit(page.getLimit())
                        .totalPages(categoryPage.getTotalPages())
                        .totalElements(categoryPage.getTotalElements())
                        .build())
                .filter(filter)
                .build();
    }

    @Override
    @Transactional
    public CreateRouteResponse createRoute(CreateRouteRequest request) {
        List<List<Double>> decodedGeometry = StridePolylineUtils.decode(request.getGeometry());
        List<List<Double>> points = mapPointsToMap(request.getSportMapType(), decodedGeometry);

        MapboxDirectionResponse mapboxResponse = mapboxService.getBatchRoute(points, request.getSportMapType());

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Geometry geometry = GeometryConverter.fromListDouble(decodedGeometry, geometryFactory);

        Optional<Route> existingRouteOpt = routeRepository.findMostSimilarRoute(geometry);
        if (existingRouteOpt.isPresent()) {
            Route route = existingRouteOpt.get();
            applyRouteUpdate(
                    route,
                    request.getActivityId(),
                    request.getImages(),
                    request.getAvgTime(),
                    request.getAvgDistance()
            );
            return new CreateRouteResponse(route.getId());
        }

        String geometryGeoJson = StridePolylineUtils.encode(mapboxResponse.getCoordinates());

        Route newRoute = createNewRoute(request, mapboxResponse, geometry, geometryGeoJson);

        return new CreateRouteResponse(newRoute.getId());
    }

    private void applyRouteUpdate(
            Route route,
            String activityId,
            List<String> images,
            Double avgTime,
            Double avgDistance
    ) {
        List<String> imagesForActivity = route.getImages().get(activityId);
        if (imagesForActivity != null) {
            imagesForActivity.addAll(images);
        } else {
            route.getImages().put(activityId, new ArrayList<>(images));
            route.setHeat(route.getHeat() + 1);
            route.setTotalTime(route.getTotalTime() + avgTime);
            route.setTotalDistance(route.getTotalDistance() + avgDistance);
        }

        routeRepository.save(route);
    }

    private Route createNewRoute(
            CreateRouteRequest request,
            MapboxDirectionResponse mapboxResponse,
            Geometry geometry,
            String geometryGeoJson
    ) {
        String mapImage = mapboxService.generateAndUpload(geometryGeoJson, "route");
        List<String> districts = getDistrictsForRoute(mapboxResponse.getCoordinates());

        String routeName = ComposeNameHelper.composeRouteName(mapboxResponse.getWayPoints());
        if (routeName == null || routeName.isEmpty()) {
            routeName = request.getWard();
        }

        Sport sport = Common.findReadOnlySportById(request.getSportId(), sportRepository, entityManager);

        Route newRoute = Route.builder()
                .sport(sport)
                .name(routeName)
                .totalDistance(request.getAvgDistance())
                .totalTime(request.getAvgDistance())
                .location(Location.builder()
                        .ward(request.getWard())
                        .district(request.getDistrict())
                        .city(request.getCity())
                        .build()
                )
                .mapImage(mapImage)
                .images(Map.of(request.getActivityId(), request.getImages() != null ? request.getImages() : new ArrayList<>()))
                .districts(districts)
                .geometry(geometry)
                .heat(1)
                .build();

        return routeRepository.save(newRoute);
    }

    private List<String> getDistrictsForRoute(List<List<Double>> coordinates) {
        FindDistrictsContainGeometryRequest request = new FindDistrictsContainGeometryRequest(
                GeometryRequest.builder()
                        .type("LineString")
                        .coordinates(coordinates)
                        .build()
        );

        FindDistrictsContainGeometryResponse response = supabaseService.findDistrictsContainGeometry(request);

        return response.getDistricts().stream()
                .map(DistrictResponse::getName)
                .toList();
    }

    private List<List<Double>> mapPointsToMap(SportMapType mapType, List<List<Double>> points) {
        List<List<Double>> simplified = RamerDouglasPeucker.handle(points, 0.0005);

        List<PointRequest> formattedData = simplified.stream()
                .map(point -> PointRequest.builder()
                        .lon(point.get(0))
                        .lat(point.get(1))
                        .build()
                ).toList();

        FindNearestWayPointsRequest request = new FindNearestWayPointsRequest(mapType.getLowercase(), formattedData);

        FindNearestWayPointsResponse responsePoints = supabaseService.findNearestWayPoints(request);

        List<WayPoint> filtered = WayPointHelper.filterPoints(responsePoints.getData());

        List<List<Double>> filteredPoints = filtered.stream()
                .map(p -> List.of(p.getLon(), p.getLat()))
                .toList();
        double epsilon = WayPointHelper.getRdpEpsilon(mapType);

        return RamerDouglasPeucker.handle(filteredPoints, epsilon);
    }

    @Override
    @Transactional
    public SaveRouteResponse saveRoute(String routeId, SaveRouteRequest request) {
        Route route = Common.findRouteById(routeId, routeRepository);
        if (route.getUserId() != null) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.CAN_NOT_SAVE_A_NON_PUBLIC_ROUTE);
        }

        String userId = SecurityUtils.getCurrentUserId();

        Route newRoute = Route.builder()
                .userId(userId)
                .sport(route.getSport())
                .name((request.getRouteName() != null && !request.getRouteName().isEmpty()) ? request.getRouteName() : route.getName())
                .totalTime(route.getTotalTime())
                .totalDistance(route.getTotalDistance())
                .location(route.getLocation())
                .mapImage(route.getMapImage())
                .images(route.getImages())
                .geometry(route.getGeometry())
                .districts(route.getDistricts())
                .heat(route.getHeat())
                .build();

        Route savedRoute = routeRepository.save(newRoute);

        return SaveRouteResponse.builder()
                .routeId(savedRoute.getId())
                .build();
    }

    @Override
    @Transactional
    public void updateRoute(String routeId, UpdateRouteRequest request) {
        Route route = Common.findRouteById(routeId, routeRepository);

        String userId = SecurityUtils.getCurrentUserId();

        if (route.getUserId() != null && !route.getUserId().equals(userId)) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.CAN_NOT_UPDATE_OTHER_USER_ROUTE);
        }

        applyRouteUpdate(
                route,
                request.getActivityId(),
                request.getImages(),
                request.getAvgTime(),
                request.getAvgDistance()
        );
    }

    @Override
    @Transactional
    public void deleteRoute(String routeId) {
        Route route = Common.findRouteById(routeId, routeRepository);

        String userId = SecurityUtils.getCurrentUserId();

        if (route.getUserId() == null) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.CAN_NOTE_DELETE_PUBLIC_ROUTE);
        } else if (!route.getUserId().equals(userId)) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.CAN_NOT_DELETE_OTHER_USER_ROUTE);
        }

        routeRepository.delete(route);
    }
}
