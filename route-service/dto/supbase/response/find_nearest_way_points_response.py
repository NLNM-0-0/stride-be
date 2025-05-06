from pydantic import BaseModel

from dto.supbase.response.way_point import WayPoint
from utils.json_format import to_camel


class FindNearestWayPointsResponse(BaseModel):
    data: list[WayPoint]

    class Config:
        alias_generator = to_camel
        populate_by_name = True