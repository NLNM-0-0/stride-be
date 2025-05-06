from pydantic import BaseModel


class FileLinkResponse(BaseModel):
    file: str