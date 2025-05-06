from typing import Optional
from uuid import UUID

from pydantic import BaseModel

from dto.route.response.location_response import LocationResponse
from utils.json_format import to_camel


class RouteShortResponse(BaseModel):
    id: UUID
    user_id: Optional[str]
    sport_id: str
    name: Optional[str]
    avg_time: Optional[float] = 0
    avg_distance: Optional[float] = 0
    map_image: Optional[str]
    location: Optional[LocationResponse]
    heat: int = 0

    class Config:
        alias_generator = to_camel
        validate_assignment = True
        populate_by_name = True