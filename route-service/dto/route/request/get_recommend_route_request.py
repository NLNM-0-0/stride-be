from pydantic import BaseModel

from utils.json_format import to_camel


class GetRecommendRouteRequest(BaseModel):
    sport_id: str
    latitude: float
    longitude: float
    limit: int

    class Config:
        alias_generator = to_camel
        validate_by_name = True