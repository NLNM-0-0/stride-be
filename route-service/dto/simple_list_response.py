from typing import TypeVar, Generic, List

from pydantic import BaseModel

from dto.app_page_request import AppPage

T = TypeVar('T')

class SimpleListResponse(BaseModel, Generic[T]):
    data: List[T]