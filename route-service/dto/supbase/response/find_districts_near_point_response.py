from pydantic import BaseModel

from dto.supbase.response.district_distance_response import DistrictDistanceResponse
from utils.json_format import to_camel


class FindDistrictNearPointResponse(BaseModel):
    data: list[DistrictDistanceResponse]

    class Config:
        alias_generator = to_camel
        populate_by_name = True