from motor.motor_asyncio import AsyncIOMotorClient, AsyncIOMotorDatabase

from configuration.manager import settings


class AsyncMongoDB:
    def __init__(self):
        self.mongo_uri: str = f"mongodb://{settings.DB_MONGO_USERNAME}:{settings.DB_MONGO_PASSWORD}@{settings.DB_MONGO_HOST}:{settings.DB_MONGO_PORT}/{settings.DB_MONGO_NAME}?authSource=admin"
        self.client: AsyncIOMotorClient = AsyncIOMotorClient(self.mongo_uri)
        self.db: AsyncIOMotorDatabase = self.client[settings.DB_MONGO_NAME]

    def close(self):
        self.client.close()


async_mongo: AsyncMongoDB = AsyncMongoDB()
