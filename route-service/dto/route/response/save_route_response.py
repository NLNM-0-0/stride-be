from uuid import UUID

from pydantic import BaseModel
from utils.json_format import to_camel


class SaveRouteResponse(BaseModel):
    route_id: str

    class Config:
        alias_generator = to_camel
        populate_by_name = True
