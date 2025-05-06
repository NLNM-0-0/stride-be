from typing import List

from dto.mapbox.response.mapbox_direction_response import MapboxWayPoint


class ComposeNameHelper:
    @staticmethod
    def compose_route_name(waypoints: List[MapboxWayPoint]) -> str:
        top_names = [w.name for w in waypoints[:min(3, len(waypoints))]]
        return " - ".join(top_names)

    @staticmethod
    def compose_location_name(ward: str, district: str, city: str) -> str:
        return ', '.join(filter(None, [ward, district, city]))