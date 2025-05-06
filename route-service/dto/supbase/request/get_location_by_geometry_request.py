from pydantic import BaseModel

from dto.supbase.request.geometry_request import GeometryRequest
from utils.json_format import to_camel


class GetGeometryByLocationRequest(BaseModel):
    geometry: GeometryRequest

    class Config:
        alias_generator = to_camel
        validate_by_name = True