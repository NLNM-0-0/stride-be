from typing import List

from clients.mapbox_client import MapboxClient
from configuration.manager import settings
from dto.mapbox.response.mapbox_direction_response import MapboxDirectionResponse
from utils.geometry_helper import GeometryHelper


class MapboxService:
    mapbox_client: MapboxClient
    access_token: str

    def __init__(self, mapbox_client: MapboxClient):
        self.mapbox_client = mapbox_client
        self.access_token = settings.mapbox_token

    def get_route(self, coordinates: List[List[float]], map_type: str = "driving") -> MapboxDirectionResponse:
        encoded_coords = GeometryHelper.encode_coordinates(coordinates)

        response = self.mapbox_client.get_directions(
            map_type=map_type,
            coordinates=encoded_coords,
            access_token=self.access_token
        )

        coordinates = response.get("routes", [])[0].get("geometry", {}).get("coordinates", [])
        waypoints = response.get("waypoints", [])
        waypoint_names_freq = {}
        for name in [waypoint.get("name") for waypoint in waypoints]:
            if name in waypoint_names_freq:
                waypoint_names_freq[name] += 1
            else:
                waypoint_names_freq[name] = 1

        return MapboxDirectionResponse(
            coordinates=coordinates,
            waypoint_names_freq=waypoint_names_freq
        )
