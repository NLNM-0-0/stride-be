from pydantic import BaseModel
from datetime import datetime


class ErrorResponse(BaseModel):
    timestamp: datetime
    status: int
    message: str
    detail: str
