from typing import Optional, List

from pydantic import BaseModel, Field

from dto.route.response.location_response import LocationResponse
from utils.json_format import to_camel
from uuid import UUID


class RouteResponse(BaseModel):
    id: str
    user_id: Optional[str]
    sport_id: str
    name: Optional[str]
    avg_time: Optional[float] = 0
    avg_distance: Optional[float] = 0
    map_image: Optional[str]
    images: List[str] = Field(default_factory=list)
    districts: Optional[List[str]]
    geometry: str
    location: Optional[LocationResponse]
    heat: int = 0

    class Config:
        alias_generator = to_camel
        validate_assignment = True
        populate_by_name = True
