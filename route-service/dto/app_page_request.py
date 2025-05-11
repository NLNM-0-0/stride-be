from pydantic import BaseModel, Field, validator

from utils.json_format import to_camel


class AppPage(BaseModel):
    page: int = Field(default=1, ge=1, description="Page number must be greater than 0")
    limit: int = Field(default=10, ge=1, description="Limit must be greater than 0")

    class Config:
        alias_generator = to_camel
        validate_by_name = True