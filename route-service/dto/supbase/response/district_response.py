from pydantic import BaseModel

from utils.json_format import to_camel


class DistrictResponse(BaseModel):
    district_name: str
    distance: float

    class Config:
        alias_generator = to_camel
        populate_by_name = True