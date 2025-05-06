from pydantic import BaseModel
from typing import List, Optional

from utils.json_format import to_camel


class UpdateRouteRequest(BaseModel):
    activity_id: Optional[str]
    avg_time: Optional[float]
    avg_distance: Optional[float]
    images: Optional[List[str]] = []

    class Config:
        alias_generator = to_camel
        validate_by_name = True
