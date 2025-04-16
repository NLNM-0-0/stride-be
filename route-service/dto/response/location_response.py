from pydantic import BaseModel


class LocationResponse(BaseModel):
    name: str
    latitude: float
    longitude: float