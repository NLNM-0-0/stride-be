from typing import List

from pydantic import BaseModel


class GeometryRequest(BaseModel):
    type: str
    coordinates: List[List[float]]