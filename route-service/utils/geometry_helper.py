from typing import List

from geoalchemy2 import WKBElement
from polyline import polyline
from shapely.geometry import LineString
from geoalchemy2.shape import from_shape

from constants.geometry_encode_type import GeometryEncodeType


class GeometryHelper:
    @staticmethod
    def encode_geometry(coordinates: list[list[float]], encode_type: GeometryEncodeType) -> str | WKBElement:
        if encode_type == GeometryEncodeType.GEOJSON:
            return GeometryHelper._encode_geojson(coordinates)
        elif encode_type == GeometryEncodeType.URL:
            return GeometryHelper._encode_str(coordinates)
        return GeometryHelper._encode_wkb(coordinates)

    @staticmethod
    def _encode_str(coordinates: List[List[float]]) -> str:
        raw_string = ";".join([f"{lng},{lat}" for lng, lat in coordinates])
        return raw_string

    @staticmethod
    def _encode_geojson(coordinates: List[List[float]]) -> str:
        return polyline.encode([(lat, lon) for lon, lat in coordinates], precision=5)

    @staticmethod
    def _encode_wkb(coordinates: List[List[float]]) -> WKBElement:
        line = LineString(coordinates)
        geometry = from_shape(line, srid=4326)
        return geometry

    @staticmethod
    def decode_geometry(geometry: str) -> list[list[float]]:
        return [[lng, lat] for lat, lng in polyline.decode(geometry, precision=5)]