from pydantic import BaseModel

from utils.json_format import to_camel


class DistrictResponse(BaseModel):
    id: str
    name: str

    class Config:
        alias_generator = to_camel
        populate_by_name = True