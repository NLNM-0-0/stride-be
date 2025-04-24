from typing import List, Dict

from pydantic import BaseModel

class Waypoint(BaseModel):
    latitude: float
    longitude: float
    name: str
    freq: int

class MapboxDirectionResponse(BaseModel):
    coordinates: List[List[float]]
    waypoints: List[Waypoint]