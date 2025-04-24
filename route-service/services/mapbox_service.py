from collections import defaultdict
from typing import List

from clients.mapbox_client import MapboxClient
from configuration.manager import settings
from dto.mapbox.response.mapbox_direction_response import MapboxDirectionResponse, Waypoint
from dto.mapbox.response.mapbox_geocoding_reverse_response import MapboxGeocodingReverseResponse
from exceptions.common_exception import StrideException
from utils.data_helper import DataHelper
from utils.geometry_helper import GeometryHelper


class MapboxService:
    mapbox_client: MapboxClient
    access_token: str

    def __init__(self, mapbox_client: MapboxClient):
        self.mapbox_client = mapbox_client
        self.access_token = settings.MAPBOX_TOKEN

    def get_batch_route(self, coordinates: List[List[float]], map_type: str = "driving") -> MapboxDirectionResponse:
        max_coords_per_request = 25

        coord_chunks = [
            coordinates[i:i + max_coords_per_request]
            for i in range(0, len(coordinates), max_coords_per_request)
        ]

        combined_coordinates = []
        combined_waypoints = []

        for chunk in coord_chunks:
            response = self.get_route(coordinates=chunk, map_type=map_type)
            combined_coordinates.extend(response.coordinates)
            combined_waypoints.extend(response.waypoints)

        combined_waypoints_dict = defaultdict(lambda: {
            "latitude": 0.0,
            "longitude": 0.0,
            "freq": 0,
            "count": 0,
            "name": "",
        })

        for wp in combined_waypoints:
            entry = combined_waypoints_dict[wp.name.lower()]
            entry["latitude"] = wp.latitude
            entry["longitude"] = wp.longitude
            entry["freq"] += wp.freq
            entry["count"] += 1
            entry["name"] = wp.name

        combined_waypoints = [
            Waypoint(
                name=data["name"],
                latitude=data["latitude"],
                longitude=data["longitude"],
                freq=data["freq"]
            )
            for _, data in combined_waypoints_dict.items()
        ]

        return MapboxDirectionResponse(
            coordinates=combined_coordinates,
            waypoints=combined_waypoints
        )

    def get_route(self, coordinates: List[List[float]], map_type: str = "driving") -> MapboxDirectionResponse:
        encoded_coords = GeometryHelper.encode_coordinates(coordinates)

        response = self.mapbox_client.get_directions(
            map_type=map_type,
            coordinates=encoded_coords,
            access_token=self.access_token
        )

        if response.status_code != 200:
            raise StrideException(
                message=f"Error fetching route data. Status code: {response.status_code}"
            )
        response = response.json()

        coordinates = DataHelper.safe_get_nested(
            response,
            ["routes", 0, "geometry", "coordinates"],
            default=""
        )

        response_waypoints = DataHelper.safe_get_nested(
            response,
            ["waypoints"],
            default=[]
        )
        waypoint_counter = {}
        for waypoint in response_waypoints:
            name = waypoint.get("name", "")
            if not name:
                continue

            if name in waypoint_counter:
                waypoint_counter[name]["freq"] += 1
            else:
                waypoint_counter[name] = {
                    "latitude": waypoint.get("location", [])[1],
                    "longitude": waypoint.get("location", [])[0],
                    "freq": 1
                }
        waypoints = [
            Waypoint(name=name, latitude=info["latitude"], longitude=info["longitude"], freq=info["freq"])
            for name, info in waypoint_counter.items()
        ]
        waypoints = sorted(waypoints, key=lambda w: w.freq, reverse=True)

        return MapboxDirectionResponse(
            coordinates=coordinates,
            waypoints=waypoints
        )

    def reverse_geocoding(self, longitude: float, latitude: float) -> MapboxGeocodingReverseResponse:
        response = self.mapbox_client.reverse_geocoding(
            longitude=longitude,
            latitude=latitude,
            limit=1,
            access_token=self.access_token
        )

        if response.status_code != 200:
            raise StrideException(
                message=f"Error reverse geocoding for latitude {latitude}, longitude {longitude}. "
                        f"Status code: {response.status_code}"
            )
        response = response.json()

        place = DataHelper.safe_get_nested(
            response,
            ["features", 0, "properties", "context", "place", "name"],
            default=""
        )
        locality = DataHelper.safe_get_nested(
            response,
            ["features", 0, "properties", "context", "locality", "name"],
            default=""
        )
        neighborhood = DataHelper.safe_get_nested(
            response,
            ["features", 0, "properties", "context", "neighborhood", "name"],
            default=""
        )

        return MapboxGeocodingReverseResponse(
            place=place,
            locality=locality,
            neighborhood=neighborhood
        )
