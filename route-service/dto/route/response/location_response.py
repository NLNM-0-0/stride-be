from pydantic import BaseModel
from typing_extensions import Optional

from utils.json_format import to_camel


class LocationResponse(BaseModel):
    ward: Optional[str]
    district: Optional[str]
    city: Optional[str]

    class Config:
        alias_generator = to_camel
        validate_assignment = True
        populate_by_name = True