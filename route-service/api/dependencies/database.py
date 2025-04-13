import typing

import loguru
from motor.motor_asyncio import AsyncIOMotorDatabase

from repositories.database import async_mongo


async def get_mongo_database() -> typing.AsyncGenerator[AsyncIOMotorDatabase, None]:
    try:
        loguru.logger.debug("Getting MongoDB session")
        yield async_mongo.db
    except Exception as e:
        loguru.logger.error(f"MongoDB error: {e}")
        raise e
