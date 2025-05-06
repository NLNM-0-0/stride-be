from pydantic import BaseModel


class MapboxWayPoint(BaseModel):
    latitude: float
    longitude: float
    name: str
    freq: int