from typing import List, Dict

from pydantic import BaseModel


class MapboxGeocodingReverseResponse(BaseModel):
    place: str
    locality: str
    neighborhood: str