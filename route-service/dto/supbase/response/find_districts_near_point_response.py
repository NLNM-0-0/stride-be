from pydantic import BaseModel

from dto.supbase.response.district_response import DistrictResponse
from utils.json_format import to_camel


class FindDistrictNearPointResponse(BaseModel):
    data: list[DistrictResponse]

    class Config:
        alias_generator = to_camel
        populate_by_name = True