from typing import Optional

from pydantic import BaseModel

from utils.json_format import to_camel


class RouteFilter(BaseModel):
    user_id: Optional[str] = None
    sport_id: Optional[str] = None

    class Config:
        alias_generator = to_camel
        validate_by_name = True
