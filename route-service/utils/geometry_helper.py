import math
from pipes import quote
from typing import List, Dict

from shapely.geometry import Polygon
import numpy as np
from shapely.geometry.point import Point
from rdp import rdp

class GeometryHelper:
    EARTH_RADIUS = 6371000  # meters

    @staticmethod
    def distance_to_point(lat_x: float, lon_x: float, lat_y: float, lon_y: float) -> float:
        r = GeometryHelper.EARTH_RADIUS
        phi1 = math.radians(lat_x)
        phi2 = math.radians(lat_y)
        delta_phi = math.radians(lat_y - lat_x)
        delta_lambda = math.radians(lon_y - lon_x)

        a = math.sin(delta_phi / 2) ** 2 + \
            math.cos(phi1) * math.cos(phi2) * math.sin(delta_lambda / 2) ** 2

        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
        return r * c

    @staticmethod
    def _latlon_to_meters(lat, lon):
        r = GeometryHelper.EARTH_RADIUS
        x = math.radians(lon) * r * math.cos(math.radians(lat))
        y = math.radians(lat) * r
        return x, y

    @staticmethod
    def distance_to_edge(lat_x, lon_x, lat_a, lon_a, lat_b, lon_b):
        x_m, y_m = GeometryHelper._latlon_to_meters(lat_x, lon_x)
        a_x_m, a_y_m = GeometryHelper._latlon_to_meters(lat_a, lon_a)
        b_x_m, b_y_m = GeometryHelper._latlon_to_meters(lat_b, lon_b)

        dx, dy = b_x_m - a_x_m, b_y_m - a_y_m
        if dx == dy == 0:
            return math.hypot(x_m - a_x_m, y_m - a_y_m)

        numerator = (x_m - a_x_m) * dx + (y_m - a_y_m) * dy
        denominator = dx * dx + dy * dy
        t = max(0.0, min(1.0, numerator / denominator))

        proj_x = a_x_m + t * dx
        proj_y = a_y_m + t * dy

        return math.hypot(x_m - proj_x, y_m - proj_y)

    @staticmethod
    def distance_to_route(lat_x, lon_y, route: List[List[float]]) -> float:
        min_distance = float("inf")
        for i in range(len(route) - 1):
            a, b = route[i], route[i + 1]
            dist = GeometryHelper.distance_to_edge(lat_x, lon_y, a[0], a[1], b[0], b[1])
            min_distance = min(min_distance, dist)
        return min_distance

    @staticmethod
    def _remove_duplicate_points(points: List[List[float]]) -> List[List[float]]:
        seen = set()
        unique = []
        for pt in points:
            t = tuple(pt)
            if t not in seen:
                unique.append(pt)
                seen.add(t)
        return unique

    @staticmethod
    def _fix_polygon(points: List[List[float]]) -> list[tuple[float, float]]:
        poly = Polygon(points)
        if not poly.is_valid:
            poly = poly.buffer(0)
        return list(poly.exterior.coords)

    @staticmethod
    def find_incenter(points: List[List[float]], resolution: int = 100) -> Dict[str, float]:
        points = GeometryHelper._fix_polygon(GeometryHelper._remove_duplicate_points(points))
        poly = Polygon(points)

        if not poly.is_valid or poly.area == 0:
            raise ValueError("Invalid polygon")

        min_x, min_y, max_x, max_y = poly.bounds
        grid_x = np.linspace(min_x, max_x, resolution)
        grid_y = np.linspace(min_y, max_y, resolution)

        best_point, best_dist = None, -1
        for x in grid_x:
            for y in grid_y:
                if not poly.contains(Point(x, y)):
                    continue
                d = min(
                    GeometryHelper.distance_to_edge(x, y, *points[i], *points[i + 1])
                    for i in range(len(points) - 1)
                )
                if d > best_dist:
                    best_point, best_dist = (x, y), d

        if not best_point:
            raise ValueError("No point found inside polygon")

        return {"latitude": best_point[0], "longitude": best_point[1]}

    @staticmethod
    def smooth_points(points: List[List[float]], epsilon: float = 0.0005) -> List[List[float]]:
        return rdp(points, epsilon=epsilon)

    @staticmethod
    def encode_coordinates(coordinates: List[List[float]]) -> str:
        raw_string = ";".join([f"{lng},{lat}" for lng, lat in coordinates])
        return quote(raw_string, safe="")
