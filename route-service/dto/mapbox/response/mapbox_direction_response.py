from typing import List, Dict

from pydantic import BaseModel


class MapboxDirectionResponse(BaseModel):
    coordinates: List[List[float]]
    waypoint_names_freq: Dict[str, dict]