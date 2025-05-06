from uuid import UUID

from pydantic import BaseModel
from utils.json_format import to_camel


class CreateRouteResponse(BaseModel):
    route_id: UUID

    class Config:
        alias_generator = to_camel
        populate_by_name = True
