import uuid
from typing import List
from uuid import UUID

from polyline import polyline
from rdp import rdp

from constants.geometry_encode_type import GeometryEncodeType
from constants.map_type import SportMapType
from dto.app_page_request import AppPage
from dto.list_response import ListResponse
from dto.route.request import route_filter
from dto.route.request.create_route_request import CreateRouteRequest
from dto.route.request.get_recommend_route_request import GetRecommendRouteRequest
from dto.route.request.route_filter import RouteFilter
from dto.route.request.update_route_request import UpdateRouteRequest
from dto.route.response.create_route_response import CreateRouteResponse
from dto.route.response.route_response import RouteResponse
from dto.route.response.save_route_response import SaveRouteResponse
from dto.simple_list_response import SimpleListResponse
from dto.supbase.request.find_districts_contain_geometry_request import FindDistrictsContainGeometryRequest
from dto.supbase.request.find_districts_near_point_request import FindDistrictNearPointRequest
from dto.supbase.request.find_nearest_way_points_request import FindNearestWayPointsRequest
from dto.supbase.request.geometry_request import GeometryRequest
from dto.supbase.request.point_request import PointRequest
from exceptions.common_exception import ResourceNotFoundException, StrideException
from mapper.route_mapper import RouteMapper
from models.route_model import RouteModel
from repositories.crud.route_repository import RouteRepository
from services.mapbox_service import MapboxService
from services.supabase_service import SupabaseService
from utils.compose_name_helper import ComposeNameHelper
from utils.geometry_helper import GeometryHelper
from utils.way_point_helper import WayPointHelper
from fastapi import status


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

    async def get_all_routes(self) -> SimpleListResponse[RouteResponse]:
        routes = await self.route_repository.get_all()

        return SimpleListResponse[RouteResponse](
            data=[RouteMapper.map_to_route_response(route) for route in routes],
        )

    async def get_recommended_routes(
            self,
            request: GetRecommendRouteRequest
    ) -> SimpleListResponse[RouteResponse]:
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
            if any(district in district_names for district in route.districts)
        ]

        result = filtered_routes[:request.limit]

        return SimpleListResponse[RouteResponse](
            data=[RouteMapper.map_to_route_response(route) for route in result],
        )


    async def get_routes(
            self,
            route_filter: RouteFilter,
            page: AppPage
    ) -> ListResponse[RouteResponse, RouteFilter]:
        routes = await self.route_repository.get_by_filters_and_paging(
            route_filter,
            page.page,
            page.limit
        )

        return ListResponse[RouteResponse, RouteFilter](
            data=[RouteMapper.map_to_route_response(route) for route in routes],
            filter=route_filter,
            page=page
        )

    async def create_route(self, user_id: str, request: CreateRouteRequest) -> CreateRouteResponse:
        user_route_id = uuid.uuid4()
        route_id = uuid.uuid4()

        print(f"Creating new_user_route with ID: {user_route_id}")
        print(f"Creating new_route with ID: {route_id}")

        map_type = SportMapType[request.sport_map_type]

        # Map geometry to the way points
        decoded_geometry = [[lng, lat] for lat, lng in polyline.decode(request.geometry, precision=5)]
        points = self._map_points_to_map(map_type=map_type, points=decoded_geometry)

        # Get route from Mapbox
        mapbox_response = self.mapbox_service.get_batch_route(coordinates=points, map_type=map_type)

        # Generate map image
        geometry = GeometryHelper.encode_geometry(
            coordinates=mapbox_response.coordinates,
            encode_type=GeometryEncodeType.GEOJSON
        )
        map_image = self.mapbox_service.generate_and_upload(geometry, f"Route_{user_route_id}")

        # Find districts
        districts_response = self.supabase_service.find_districts_contain_geometry(
            FindDistrictsContainGeometryRequest(
                geometry=GeometryRequest(
                    type="LineString",
                    coordinates=mapbox_response.coordinates
                )
            )
        )
        districts = [district.name for district in districts_response.districts]

        # Generate route name
        public_route_name = (ComposeNameHelper.compose_route_name(mapbox_response.waypoints)
                      or request.ward)

        # Create and save user route
        new_user_route = RouteModel(
            id=user_route_id,
            user_id=user_id,
            sport_id=request.sport_id,
            name=request.route_name,
            total_time=request.avg_time,
            total_distance=request.avg_distance,
            location={
                "ward": request.ward,
                "district": request.district,
                "city": request.city,
            },
            map_image=map_image,
            images={request.activity_id: request.images or []},
            districts=districts,
            geometry=geometry,
            heat=1,
        )
        await self.route_repository.insert_one(new_user_route)

        # Create and save route
        new_route = RouteModel(
            id = route_id,
            sport_id=request.sport_id,
            name=public_route_name,
            total_time=request.avg_time,
            total_distance=request.avg_distance,
            location={
                "ward": request.ward,
                "district": request.district,
                "city": request.city,
            },
            map_image=map_image,
            images={request.activity_id: request.images or []},
            districts=districts,
            geometry=geometry,
            heat=1,
        )
        await self.route_repository.insert_one(new_route)

        return CreateRouteResponse(route_id=str(user_route_id))

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

    async def save_route(self, user_id: str, route_id: str) -> SaveRouteResponse:
        route = await self._get_route_by_id(route_id)

        if route.user_id == user_id:
            raise StrideException(
                status_code=status.HTTP_400_BAD_REQUEST,
                message="You cannot save a route twice"
            )

        route_id = uuid.uuid4()
        route.id = route_id
        route.user_id = user_id

        new_route = await self.route_repository.insert_one(route)

        return SaveRouteResponse(route_id=str(new_route.id))

    async def update_route(self, user_id: str, route_id: str, request: UpdateRouteRequest):
        route = await self._get_route_by_id(route_id)

        if route.user_id != None and route.user_id != user_id:
            raise StrideException(status_code=status.HTTP_400_BAD_REQUEST, message="Can not update other user route")

        if request.activity_id in route.images:
            route.images[request.activity_id].extend(request.images)
        else:
            route.images[request.activity_id] = request.images
            route.heat += 1
            route.total_time += request.avg_time
            route.total_distance += request.avg_distance

        update_data = {
            "images": route.images,
            "heat": route.heat,
            "total_time": route.total_time,
            "total_distance": route.total_distance,
        }

        await self.route_repository.update_route(route, update_data)

    async def _get_route_by_id(self, route_id: str) -> RouteModel:
        try:
            route_uuid = UUID(route_id)
        except ValueError:
            raise ResourceNotFoundException("Route", "id", route_id)

        route = await self.route_repository.get_by_id(route_uuid)
        if not route:
            raise ResourceNotFoundException("Route", "id", route_id)

        return route

    async def delete_route(self, user_id: str, route_id: str):
        route = await self._get_route_by_id(route_id)

        if route.user_id == None:
            raise StrideException(status_code=status.HTTP_400_BAD_REQUEST, message="Can not delete public route")
        elif route.user_id != user_id:
            raise StrideException(status_code=status.HTTP_400_BAD_REQUEST, message="Can not delete user route")

        await self.route_repository.delete(route)
