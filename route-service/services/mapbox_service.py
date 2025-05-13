from urllib.parse import quote
from collections import defaultdict
from typing import List

import requests
from fastapi import status

from clients.bridge_client import BridgeClient
from clients.mapbox_client import MapboxClient
from configuration.manager import settings
from constants.geometry_encode_type import GeometryEncodeType
from constants.map_type import SportMapType
from dto.mapbox.response.mapbox_direction_response import MapboxWayPoint, MapboxDirectionResponse
from exceptions.common_exception import StrideException
from utils.data_helper import DataHelper
from utils.geometry_helper import GeometryHelper


class MapboxService:
    def __init__(self, mapbox_client: MapboxClient, bridge_client: BridgeClient):
        self.mapbox_client = mapbox_client
        self.bridge_client = bridge_client
        self.default_style = settings.MAPBOX_STYLE
        self.default_stroke_width = settings.MAPBOX_STROKE_WIDTH
        self.default_stroke_color = settings.MAPBOX_STROKE_COLOR
        self.default_stroke_fill = settings.MAPBOX_STROKE_FILL
        self.default_width = settings.MAPBOX_WIDTH
        self.default_height = settings.MAPBOX_HEIGHT
        self.default_padding = settings.MAPBOX_PADDING
        self.content_type = settings.MAPBOX_CONTENT_TYPE

    def get_batch_route(
            self,
            coordinates: List[List[float]],
            map_type: SportMapType = SportMapType.DRIVING
    ) -> MapboxDirectionResponse:
        max_coords_per_request = 25
        coord_chunks = [coordinates[i:i + max_coords_per_request] for i in
                        range(0, len(coordinates), max_coords_per_request)]

        combined_coordinates, combined_waypoints = [], []
        for chunk in coord_chunks:
            result = self.get_route(chunk, map_type)
            combined_coordinates.extend(result.coordinates)
            combined_waypoints.extend(result.waypoints)

        # Group waypoints by name
        waypoint_dict = defaultdict(lambda: {
            "latitude": 0.0,
            "longitude": 0.0,
            "freq": 0,
            "name": ""
        })
        for wp in combined_waypoints:
            key = wp.name.lower()
            waypoint_dict[key]["latitude"] = wp.latitude
            waypoint_dict[key]["longitude"] = wp.longitude
            waypoint_dict[key]["freq"] += wp.freq
            waypoint_dict[key]["name"] = wp.name

        waypoints = [MapboxWayPoint(**val) for val in waypoint_dict.values()]
        return MapboxDirectionResponse(coordinates=combined_coordinates, waypoints=waypoints)

    def get_route(
            self,
            coordinates: List[List[float]],
            map_type: SportMapType = SportMapType.DRIVING
    ) -> MapboxDirectionResponse:
        encoded_coords = GeometryHelper.encode_geometry(
            coordinates=coordinates,
            encode_type=GeometryEncodeType.URL
        )
        response = self.mapbox_client.get_directions(
            map_type=map_type.lowercase,
            coordinates=encoded_coords
        )

        coords = DataHelper.safe_get_nested(
            response,
            ["routes", 0, "geometry", "coordinates"],
            default=[]
        )
        raw_wps = response.get("waypoints", [])
        counter = {}
        for wp in raw_wps:
            name = wp.get("name", "")
            if not name:
                continue
            if name in counter:
                counter[name]["freq"] += 1
            else:
                counter[name] = {
                    "latitude": wp["location"][1],
                    "longitude": wp["location"][0],
                    "freq": 1,
                }

        waypoints = [MapboxWayPoint(
            name=k,
            latitude=v["latitude"],
            longitude=v["longitude"],
            freq=v["freq"]
        ) for k, v in counter.items()]
        waypoints.sort(key=lambda w: w.freq, reverse=True)
        return MapboxDirectionResponse(coordinates=coords, waypoints=waypoints)

    def generate_and_upload(self, path: str, file_name: str) -> str:
        image_data = self._generate_image(path)
        return self._upload_file(image_data, file_name)

    def _generate_image(self, path: str) -> bytes:
        return self._generate_image_with_params(
            path,
            self.default_style,
            self.default_stroke_width,
            self.default_stroke_color,
            self.default_stroke_fill,
            self.default_width,
            self.default_height,
            self.default_padding,
        )

    def _generate_image_with_params(
            self,
            path: str,
            style: str,
            stroke_width: str,
            stroke_color: str,
            stroke_fill: str,
            width: int,
            height: int,
            padding: int
    ) -> bytes:
        try:
            encoded_path = quote(path.encode('utf-8'), safe="")

            return self.mapbox_client.get_static_map_image(
                map_style=style,
                stroke_width=stroke_width,
                stroke_color=stroke_color,
                stroke_fill=stroke_fill,
                path=encoded_path,
                width=width,
                height=height,
                padding=padding
            )
        except requests.HTTPError as e:
            raise StrideException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                message=f"Failed to generate image: {e}"
            )

    def _upload_file(self, data: bytes, name: str) -> str:
        try:
            response = self.bridge_client.upload_raw_file(
                data=data,
                file_name=name,
                content_type=self.content_type
            )
            return response.file
        except requests.HTTPError as e:
            raise StrideException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                message=f"Failed to upload image: {e}"
            )
