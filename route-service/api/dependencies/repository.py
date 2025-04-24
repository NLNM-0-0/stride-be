import typing
import fastapi
from motor.motor_asyncio import AsyncIOMotorDatabase

from api.dependencies.database import get_mongo_database
from repositories.base import BaseMongoRepository


def get_repository(
    repo_type: typing.Type[BaseMongoRepository],
) -> typing.Callable[[AsyncIOMotorDatabase], BaseMongoRepository]:
    def _get_repo(
        db: AsyncIOMotorDatabase = fastapi.Depends(get_mongo_database),
    ) -> BaseMongoRepository:
        return repo_type(db=db)

    return _get_repo