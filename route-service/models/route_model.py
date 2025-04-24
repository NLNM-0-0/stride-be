from bson import ObjectId
from pydantic import BaseModel, Field, GetCoreSchemaHandler
from pydantic_core import core_schema
from typing import List, Optional, Any, Dict


class PyObjectId(ObjectId):
    @classmethod
    def __get_pydantic_core_schema__(cls, source_type: Any, handler: GetCoreSchemaHandler) -> core_schema.CoreSchema:
        return core_schema.no_info_plain_validator_function(cls.validate)

    @classmethod
    def validate(cls, v):
        if isinstance(v, ObjectId):
            return str(v)
        if ObjectId.is_valid(v):
            return str(ObjectId(v))
        raise ValueError("Invalid ObjectId")

class RouteModel(BaseModel):
    id: Optional[PyObjectId] = Field(default_factory=PyObjectId, alias="_id")
    sport_id: Optional[str]
    name: Optional[str]
    avg_time: Optional[float] = 0
    total_time: Optional[float] = 0
    location: Optional[str]
    images: Optional[Dict[str, List[str]]] = {}
    coordinates: Optional[List[List[float]]] = []
    localities: Optional[List[str]] = []
    heat: Optional[int] = 0

    class Config:
        validate_by_name = True
        arbitrary_types_allowed = True
        json_encoders = {ObjectId: str}
