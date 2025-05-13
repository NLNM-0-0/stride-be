from typing import TypeVar, Generic, List

from pydantic import BaseModel

from dto.page.app_page_response import AppPageResponse

T = TypeVar('T')
F = TypeVar('F')

class ListResponse(BaseModel, Generic[T, F]):
    data: List[T]
    filter: F
    page: AppPageResponse