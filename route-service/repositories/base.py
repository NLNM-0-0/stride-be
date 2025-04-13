from motor.motor_asyncio import AsyncIOMotorDatabase

class BaseMongoRepository:
    def __init__(self, db: AsyncIOMotorDatabase):
        self.db = db