from pydantic import BaseModel
from typing import List, Optional

from constants.map_type import SportMapType
from utils.json_format import to_camel


class CreateRouteRequest(BaseModel):
    sport_id: str
    activity_id: str
    sport_map_type: str
    avg_time: Optional[float]
    avg_distance: Optional[float]
    images: Optional[List[str]] = []
    geometry: Optional[str]

    class Config:
        alias_generator = to_camel
        validate_by_name = True
