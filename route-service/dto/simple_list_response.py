from typing import TypeVar, Generic, List

from pydantic import BaseModel

T = TypeVar('T')

class SimpleListResponse(BaseModel, Generic[T]):
    data: List[T]