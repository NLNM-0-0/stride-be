from motor.motor_asyncio import AsyncIOMotorClient, AsyncIOMotorDatabase

from configuration.manager import settings


class AsyncMongoDB:
    def __init__(self):
        self.mongo_uri: str = settings.DB_MONGO_URL
        self.client: AsyncIOMotorClient = AsyncIOMotorClient(self.mongo_uri)
        self.db: AsyncIOMotorDatabase = self.client[settings.DB_MONGO_NAME]

    def close(self):
        self.client.close()


async_mongo: AsyncMongoDB = AsyncMongoDB()
