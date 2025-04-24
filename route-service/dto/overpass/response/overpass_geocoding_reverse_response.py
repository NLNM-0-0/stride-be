from pydantic import BaseModel


class OverpassGeocodingReverseResponse(BaseModel):
    place: str
    locality: str
    neighborhood: str