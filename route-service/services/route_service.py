import uuid
from typing import List
from uuid import UUID

from polyline import polyline
from rdp import rdp

from constants.geometry_encode_type import GeometryEncodeType
from constants.map_type import SportMapType
from dto.app_page_request import AppPageRequest
from dto.route.request.create_route_request import CreateRouteRequest
from dto.route.request.get_recommend_route_request import GetRecommendRouteRequest
from dto.route.request.route_filter import RouteFilter
from dto.route.request.update_route_request import UpdateRouteRequest
from dto.route.response.create_route_response import CreateRouteResponse
from dto.route.response.route_response import RouteResponse
from dto.route.response.route_short_response import RouteShortResponse
from dto.supbase.request.find_districts_near_point_request import FindDistrictNearPointRequest
from dto.supbase.request.find_nearest_way_points_request import FindNearestWayPointsRequest
from dto.supbase.request.geometry_request import GeometryRequest
from dto.supbase.request.get_location_by_geometry_request import GetGeometryByLocationRequest
from dto.supbase.request.point_request import PointRequest
from dto.supbase.response.get_location_by_geometry_response import GetLocationByGeometryResponse
from exceptions.common_exception import ResourceNotFoundException
from mapper.route_mapper import RouteMapper
from models.route_model import RouteModel
from repositories.crud.route_repository import RouteRepository
from services.mapbox_service import MapboxService
from services.supabase_service import SupabaseService
from utils.compose_name_helper import ComposeNameHelper
from utils.geometry_helper import GeometryHelper
from utils.way_point_helper import WayPointHelper


class RouteService:
    route_repository: RouteRepository
    mapbox_service: MapboxService
    supabase_service: SupabaseService

    def __init__(
            self,
            route_repository: RouteRepository,
            mapbox_service: MapboxService,
            supabase_service: SupabaseService):
        self.route_repository = route_repository
        self.mapbox_service = mapbox_service
        self.supabase_service = supabase_service

    async def get_all_routes(self) -> List[RouteShortResponse]:
        routes = await self.route_repository.get_all()
        return [RouteMapper.map_to_route_short_response(route) for route in routes]

    async def get_recommended_routes(
            self,
            request: GetRecommendRouteRequest
    ) -> List[RouteShortResponse]:
        routes = await self.route_repository.get_by_filters(
            route_filter=RouteFilter(sport_id=request.sport_id, user_id=None)
        )

        map_type = SportMapType[request.sport_map_type]
        districts = self.supabase_service.find_districts_near_point(FindDistrictNearPointRequest(
            lat=request.latitude,
            lon=request.longitude,
            around=map_type.recommended_distance
        ))
        district_names = [district.district_name for district in districts.data]

        filtered_routes = [
            route for route in routes
            if route.district in district_names
        ]

        result = filtered_routes[:request.limit]

        return [RouteMapper.map_to_route_short_response(route) for route in result]

    async def get_routes(
            self,
            route_filter: RouteFilter,
            page: AppPageRequest
    ) -> List[RouteShortResponse]:
        routes = await self.route_repository.get_by_filters_and_paging(
            route_filter,
            page.page,
            page.limit
        )
        return [RouteMapper.map_to_route_short_response(route) for route in routes]

    async def get_route(self, route_id: str) -> RouteResponse:
        # Convert string ID to UUID safely
        try:
            route_uuid = UUID(route_id)
        except ValueError:
            raise ResourceNotFoundException("Route", "id", route_id)

        # Fetch the route
        route = await self.route_repository.get_by_id(route_uuid)
        if not route:
            raise ResourceNotFoundException("Route", "id", route_id)

        return RouteMapper.map_to_route_response(route)


    async def create_route(self, user_id: str, request: CreateRouteRequest) -> CreateRouteResponse:
        route_id = uuid.uuid4()
        map_type = SportMapType[request.sport_map_type]

        # Map geometry to the way points
        decoded_geometry = [[lng, lat] for lat, lng in polyline.decode(request.geometry, precision=5)]
        points = self._map_points_to_map(map_type=map_type, points=decoded_geometry)

        # Get route from Mapbox
        mapbox_response = self.mapbox_service.get_batch_route(coordinates=points, map_type=map_type)

        # Get location from geometry
        location = self._fetch_location_from_geometry(mapbox_response.coordinates)
        location_name = ComposeNameHelper.compose_location_name(
            ward=location.ward,
            district=location.district,
            city=location.city,
        )

        # Generate map image
        geometry = GeometryHelper.encode_geometry(
            coordinates=mapbox_response.coordinates,
            encode_type=GeometryEncodeType.GEOJSON
        )
        map_image = self.mapbox_service.generate_and_upload(geometry, f"Route_{route_id}")

        # Generate route name
        route_name = (ComposeNameHelper.compose_route_name(mapbox_response.waypoints)
                      or location.ward)

        # Create and save user route
        new_user_route = RouteModel(
            id=route_id,
            user_id=user_id,
            sport_id=request.sport_id,
            name=route_name,
            total_time=request.avg_time,
            total_distance=request.avg_distance,
            location=location_name,
            map_image=map_image,
            images={request.activity_id: request.images or []},
            district=location.district,
            geometry=geometry,
            heat=1,
        )
        new_user_route = await self.route_repository.insert_one(new_user_route)

        # Create and save route
        new_route = RouteModel(
            sport_id=request.sport_id,
            name=route_name,
            total_time=request.avg_time,
            total_distance=request.avg_distance,
            location=location_name,
            map_image=map_image,
            images={request.activity_id: request.images or []},
            district=location.district,
            geometry=geometry,
            heat=1,
        )
        await self.route_repository.insert_one(new_route)

        return CreateRouteResponse(route_id=new_user_route.id)

    def _map_points_to_map(self, map_type: SportMapType, points: list[list[float]]) -> list[list[float]]:
        count = WayPointHelper.get_simplification_step(map_type=map_type)
        simplified = points[::count]

        formatted_data = [PointRequest(
            lat=lat,
            lon=lon
        ) for lon, lat in simplified]

        request = FindNearestWayPointsRequest(
            data=formatted_data,
            type=map_type.lowercase,
        )
        response = self.supabase_service.find_nearest_way_points(request)

        filtered = WayPointHelper.filter_points(response.data)
        filtered_points = [[p.lon, p.lat] for p in filtered]

        epsilon = WayPointHelper.get_rdp_epsilon(map_type=map_type)
        return rdp(filtered_points, epsilon=epsilon)

    def _fetch_location_from_geometry(self, coordinates: list[list[float]]) -> GetLocationByGeometryResponse:
        return self.supabase_service.get_location_by_geometry(
            GetGeometryByLocationRequest(
                geometry=GeometryRequest(
                    type="LineString",
                    coordinates=coordinates
                )
            )
        )

    async def update_route(self, route_id: str, request: UpdateRouteRequest):
        # Convert string ID to UUID safely
        try:
            route_uuid = UUID(route_id)
        except ValueError:
            raise ResourceNotFoundException("Route", "id", route_id)

        # Fetch the route
        route = await self.route_repository.get_by_id(route_uuid)
        if not route:
            raise ResourceNotFoundException("Route", "id", route_id)

        # Update images and heat/avg_time
        if request.activity_id in route.images:
            route.images[request.activity_id].extend(request.images)
        else:
            route.images[request.activity_id] = request.images
            route.heat += 1
            route.total_time += request.avg_time
            route.total_distance += request.avg_distance

        # Prepare data for update (skip id)
        update_data = {
            "images": route.images,
            "heat": route.heat,
            "total_time": route.total_time,
            "total_distance": route.total_distance,
        }

        # Apply update
        await self.route_repository.update_by_id(route_uuid, update_data)
