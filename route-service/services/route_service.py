from typing import List

from bson import ObjectId

from dto.request.create_route_request import CreateRouteRequest
from dto.request.get_recommend_route_request import GetRecommendRouteRequest
from dto.request.update_route_request import UpdateRouteRequest
from dto.response.route_response import RouteResponse
from exceptions.common_exception import ResourceNotFoundException
from repositories.crud.route_repository import RouteRepository
from utils.geometry_helper import GeometryHelper


class RouteService:
    def __init__(self, route_repository: RouteRepository):
        self.route_repository = route_repository

    async def get_all_routes(self) -> List[RouteResponse]:
        routes = await self.route_repository.get_all()
        return [RouteResponse.model_validate(route.model_dump(by_alias=True)) for route in routes]

    async def get_recommended_routes(self, request: GetRecommendRouteRequest) -> List[RouteResponse]:
        # Fetch all routes that match the given sport_id
        routes = await self.route_repository.get_by_sport_id(request.sport_id)

        near_routes = []
        for route in routes:
            # Calculate the distance from the user's location to the route's starting location
            distance = GeometryHelper.distance_to_point(
                request.longitude, request.latitude,
                route.location.longitude, route.location.latitude
            )
            # Only include routes that are within 5km of the user
            if distance < 5000:
                near_routes.append(route)

        # Sort nearby routes by heat
        near_routes.sort(key=lambda r: r.heat, reverse=True)


        # Limit the number of recommended routes based on the request
        near_routes = near_routes[:request.limit]

        # Convert each route model to the RouteResponse DTO with alias handling (e.g., _id -> id)
        return [RouteResponse.model_validate(route.model_dump(by_alias=True)) for route in near_routes]

    async def create_route(self, request: CreateRouteRequest):
        points = request.coordinates

        points = GeometryHelper.smooth_points(points)

        incenter = GeometryHelper.find_incenter(points)

        route_data = {
            "sport_id": ObjectId(request.sport_id),
            "name": request.name,
            "avg_time": request.avg_time,
            "total_time": request.avg_time,
            "location": {
                "name": request.location_name,
                "latitude": incenter["latitude"],
                "longitude": incenter["longitude"],
            },
            "images": request.images if request.images else [],
            "coordinates": points,
            "heat": 1
        }

        await self.route_repository.insert_one(route_data)

    async def update_route(self, route_id: str, request: UpdateRouteRequest):
        route = await self.route_repository.get_by_id(route_id)
        if not route:
            raise ResourceNotFoundException("Route", "id", route_id)

        route.heat += 1
        route.total_time += request.avg_time
        route.avg_time = route.total_time / route.heat

        route.images.extend(request.images)

        update_data = route.model_dump(exclude={"id"})

        await self.route_repository.update_by_id(route_id, update_data)
