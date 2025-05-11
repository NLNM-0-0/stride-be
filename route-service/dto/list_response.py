from typing import TypeVar, Generic, List

from pydantic import BaseModel

from dto.app_page_request import AppPage

T = TypeVar('T')
F = TypeVar('F')

class ListResponse(BaseModel, Generic[T, F]):
    data: List[T]
    filter: F
    page: AppPage