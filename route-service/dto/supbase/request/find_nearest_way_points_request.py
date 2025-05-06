from pydantic import BaseModel

from dto.supbase.request.point_request import PointRequest
from utils.json_format import to_camel


class FindNearestWayPointsRequest(BaseModel):
    type: str
    data: list[PointRequest]

    class Config:
        alias_generator = to_camel
        validate_by_name = True