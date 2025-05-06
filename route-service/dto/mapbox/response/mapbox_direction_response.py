from typing import List, Dict

from pydantic import BaseModel

from dto.mapbox.response.mapbox_way_point import MapboxWayPoint


class MapboxDirectionResponse(BaseModel):
    coordinates: List[List[float]]
    waypoints: List[MapboxWayPoint]