import asyncio
from collections import Counter
from typing import List

from bson import ObjectId

from dto.mapbox.response.mapbox_direction_response import Waypoint
from dto.mapbox.response.mapbox_geocoding_reverse_response import MapboxGeocodingReverseResponse
from dto.route.request.create_route_request import CreateRouteRequest
from dto.route.request.get_recommend_route_request import GetRecommendRouteRequest
from dto.route.request.update_route_request import UpdateRouteRequest
from dto.route.response.create_route_response import CreateRouteResponse
from dto.route.response.route_response import RouteResponse
from exceptions.common_exception import ResourceNotFoundException
from repositories.crud.route_repository import RouteRepository
from services.mapbox_service import MapboxService
from services.overpass_service import OverpassService


class RouteService:
    route_repository: RouteRepository
    mapbox_service: MapboxService
    overpass_service: OverpassService

    def __init__(
            self,
            route_repository: RouteRepository,
            mapbox_service: MapboxService,
            overpass_service: OverpassService):
        self.route_repository = route_repository
        self.mapbox_service = mapbox_service
        self.overpass_service = overpass_service

    async def get_all_routes(self) -> List[RouteResponse]:
        routes = await self.route_repository.get_all()
        route_responses = []

        for route in routes:
            route_dict = route.model_dump(by_alias=True)

            raw_images = route_dict.get("images", {})
            route_dict["images"] = raw_images.get("map", [])

            route_responses.append(RouteResponse.model_validate(route_dict))

        return route_responses

    async def get_recommended_routes(self, request: GetRecommendRouteRequest) -> List[RouteResponse]:
        routes = await self.route_repository.get_by_sport_id(request.sport_id)

        localities = self.overpass_service.fetch_nearby_localities(
            latitude=request.latitude,
            longitude=request.longitude,
            around=request.around
        )

        filtered_routes = [
            route for route in routes
            if any(locality in localities for locality in route.localities)
        ]

        result = filtered_routes[:request.limit]

        route_responses = []

        for route in result:
            route_dict = route.model_dump(by_alias=True)

            raw_images = route_dict.get("images", {})
            route_dict["images"] = raw_images.get("map", [])

            route_responses.append(RouteResponse.model_validate(route_dict))

        return route_responses

    async def create_route(self, request: CreateRouteRequest) -> CreateRouteResponse:
        points = request.coordinates
        max_coords_per_request = 25

        coord_chunks = [
            points[i:i + max_coords_per_request]
            for i in range(0, len(points), max_coords_per_request)
        ]

        combined_coordinates = []
        combined_waypoints = []

        for chunk in coord_chunks:
            response = self.mapbox_service.get_route(chunk, request.sport_map_type)
            combined_coordinates.extend(response.coordinates)
            combined_waypoints.extend(response.waypoints)

        unique_localities, places = self._sort_localities_by_frequency(combined_waypoints)
        location = places[0].neighborhood + ", " + places[0].locality + ", " + places[0].place

        route_data = {
            "sport_id": request.sport_id,
            "name": self._generate_route_name(combined_waypoints),
            "avg_time": request.avg_time,
            "total_time": request.avg_time,
            "location": location,
            "images": {request.activity_id: request.images if request.images else []},
            "localities": unique_localities,
            "coordinates": combined_coordinates,
            "heat": 1
        }

        _id = await self.route_repository.insert_one(route_data)

        return CreateRouteResponse.model_validate({"route_id": str(_id)})

    def _generate_route_name(self, waypoints: List[Waypoint]) -> str:
        top_names = [w.name for w in waypoints[:min(3, len(waypoints))]]
        return " - ".join(top_names)

    def _sort_localities_by_frequency(self, waypoints: List[Waypoint]):
        geocoding_results = {}

        for waypoint in waypoints:
            geocoding_results[(waypoint.latitude, waypoint.longitude)] = self.mapbox_service.reverse_geocoding(
                latitude=waypoint.latitude,
                longitude=waypoint.longitude
            )

        locality_counter = Counter()

        for waypoint in waypoints:
            geocoding_response = geocoding_results[(waypoint.latitude, waypoint.longitude)]
            locality_counter[geocoding_response.locality] += 1

        sorted_localities = sorted(locality_counter.items(), key=lambda x: x[1], reverse=True)

        sorted_responses = [
            MapboxGeocodingReverseResponse(
                place=geocoding_results[(waypoint.latitude, waypoint.longitude)].place,
                locality=locality,
                neighborhood=geocoding_results[(waypoint.latitude, waypoint.longitude)].neighborhood
            )
            for locality, freq in sorted_localities
            for waypoint in waypoints
            if geocoding_results[(waypoint.latitude, waypoint.longitude)].locality == locality
        ]

        return list(locality_counter.keys()), sorted_responses

    async def update_route(self, route_id: str, request: UpdateRouteRequest):
        route = await self.route_repository.get_by_id(route_id)
        if not route:
            raise ResourceNotFoundException("Route", "id", route_id)

        if request.activity_id in route.images:
            route.images[request.activity_id].extend(request.images)
        else:
            route.images[request.activity_id] = request.images
            route.heat += 1
            route.total_time += request.avg_time
            route.avg_time = route.total_time / route.heat

        update_data = route.model_dump(exclude={"id"})

        await self.route_repository.update_by_id(route_id, update_data)
