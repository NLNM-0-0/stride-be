import asyncio
from collections import Counter, defaultdict
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

        mapbox_response = self.mapbox_service.get_batch_route(
            coordinates=points,
            map_type=request.sport_map_type
        )

        unique_localities, places = self._sort_localities_by_frequency(mapbox_response.waypoints)
        location = places[0].neighborhood + ", " + places[0].locality + ", " + places[0].place

        route_data = {
            "sport_id": request.sport_id,
            "name": self._generate_route_name(mapbox_response.waypoints),
            "avg_time": request.avg_time,
            "total_time": request.avg_time,
            "location": location,
            "images": {request.activity_id: request.images if request.images else []},
            "localities": unique_localities,
            "coordinates": mapbox_response.coordinates,
            "heat": 1
        }

        _id = await self.route_repository.insert_one(route_data)

        return CreateRouteResponse.model_validate({"route_id": str(_id)})

    def  _generate_route_name(self, waypoints: List[Waypoint]) -> str:
        top_names = [w.name for w in waypoints[:min(3, len(waypoints))]]
        return " - ".join(top_names)

    def _sort_localities_by_frequency(self, waypoints: List[Waypoint]):
        geocoding_results = {}

        for waypoint in waypoints:
            geocoding_results[(waypoint.latitude, waypoint.longitude)] = self.overpass_service.reverse_geocoding(
                latitude=waypoint.latitude,
                longitude=waypoint.longitude
            )

        grouped_counter = defaultdict(lambda: {
            "place": "None",
            "locality": "None",
            "neighborhood": "None",
            "freq": 0
        })

        for waypoint in waypoints:
            geocoding = geocoding_results[(waypoint.latitude, waypoint.longitude)]
            key = (geocoding.place, geocoding.locality, geocoding.neighborhood)
            grouped_counter[key]["place"] = geocoding.place
            grouped_counter[key]["locality"] = geocoding.locality
            grouped_counter[key]["neighborhood"] = geocoding.neighborhood
            grouped_counter[key]["freq"] += 1

        sorted_grouped = sorted(grouped_counter.values(), key=lambda x: x["freq"], reverse=True)

        return (
            list(set([entry["locality"] for entry in sorted_grouped])),
            [
                MapboxGeocodingReverseResponse(
                    place=entry["place"],
                    locality=entry["locality"],
                    neighborhood=entry["neighborhood"]
                )
                for entry in sorted_grouped
            ]
        )

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
