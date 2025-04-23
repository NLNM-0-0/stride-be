from pydantic import BaseModel


class SimpleResponse(BaseModel):
    data: bool = True
