from pydantic import BaseModel

from utils.json_format import to_camel


class AppPageResponse(BaseModel):
    page: int
    limit: int
    total_elements: int
    total_pages: int

    class Config:
        alias_generator = to_camel
        validate_by_name = True