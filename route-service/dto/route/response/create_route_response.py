from bson import ObjectId
from pydantic import BaseModel

from utils.json_format import to_camel


class CreateRouteResponse(BaseModel):
    route_id: str

    class Config:
        alias_generator = to_camel
        validate_by_name = True
        json_encoders = {
            ObjectId: str
        }
