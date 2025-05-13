from pydantic import BaseModel
from typing import Optional

from utils.json_format import to_camel


class SaveRouteRequest(BaseModel):
    route_name: Optional[str]

    class Config:
        alias_generator = to_camel
        validate_by_name = True
