import typing
import fastapi
from sqlalchemy.ext.asyncio import AsyncSession

from api.dependencies.database import get_postgres_session
from repositories.base import BaseSQLRepository


def get_repository(
    repo_type: typing.Type[BaseSQLRepository],
) -> typing.Callable[[AsyncSession], BaseSQLRepository]:
    def _get_repo(
        db: AsyncSession = fastapi.Depends(get_postgres_session),
    ) -> BaseSQLRepository:
        return repo_type(db)

    return _get_repo
