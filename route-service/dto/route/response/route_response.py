from typing import Optional, List

from bson import ObjectId
from pydantic import BaseModel, Field

from utils.json_format import to_camel


class RouteResponse(BaseModel):
    id: str = Field(..., alias="_id")
    sport_id: str
    name: Optional[str]
    avg_time: Optional[float] = 0
    total_time: Optional[float] = 0
    location: Optional[str]
    images: List[str] = []
    coordinates: List[List[float]] = []
    heat: int = 0

    class Config:
        alias_generator = to_camel
        validate_by_name = True
        json_encoders = {
            ObjectId: str
        }