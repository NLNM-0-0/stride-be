from pydantic import BaseModel
from typing import List, Optional

from utils.json_format import to_camel


class CreateRouteRequest(BaseModel):
    sport_id: str
    activity_id: str
    sport_map_type: str
    avg_time: Optional[float]
    images: Optional[List[str]] = []
    coordinates: Optional[List[List[float]]] = []

    class Config:
        alias_generator = to_camel
        validate_by_name = True
