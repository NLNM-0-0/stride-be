from typing import List

from polyline import polyline

from constants.geometry_encode_type import GeometryEncodeType


class GeometryHelper:
    @staticmethod
    def encode_geometry(coordinates: list[list[float]], encode_type: GeometryEncodeType) -> str:
        if encode_type == GeometryEncodeType.GEOJSON:
            return GeometryHelper._encode_geojson(coordinates)
        return GeometryHelper._encode_str(coordinates)

    @staticmethod
    def _encode_str(coordinates: List[List[float]]) -> str:
        raw_string = ";".join([f"{lng},{lat}" for lng, lat in coordinates])
        return raw_string

    @staticmethod
    def _encode_geojson(coordinates: List[List[float]]) -> str:
        return polyline.encode([(lon, lat) for lon, lat in coordinates], precision=5, geojson=True)