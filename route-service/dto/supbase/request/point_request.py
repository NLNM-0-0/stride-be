from pydantic import BaseModel

from utils.json_format import to_camel


class PointRequest(BaseModel):
    lat: float
    lon: float

    class Config:
        alias_generator = to_camel
        validate_by_name = True