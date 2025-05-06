from pydantic import BaseModel

from utils.json_format import to_camel


class FindDistrictNearPointRequest(BaseModel):
    lat: float
    lon: float
    around: int

    class Config:
        alias_generator = to_camel
        validate_by_name = True