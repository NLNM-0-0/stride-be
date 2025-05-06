from pydantic import BaseModel

from utils.json_format import to_camel


class WayPoint(BaseModel):
    name: str
    lat: float
    lon: float

    class Config:
        alias_generator = to_camel
        populate_by_name = True