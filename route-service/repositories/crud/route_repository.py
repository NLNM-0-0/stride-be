from motor.motor_asyncio import AsyncIOMotorDatabase
from bson import ObjectId
from typing import List, Optional

from models.route_model import RouteModel
from repositories.base import BaseMongoRepository


class RouteRepository(BaseMongoRepository):
    def __init__(self, db: AsyncIOMotorDatabase):
        super().__init__(db)
        self.collection = self.db.get_collection("routes")

    async def get_all(self) -> List[RouteModel]:
        routes_cursor = self.collection.find()
        routes = []
        async for route in routes_cursor:
            routes.append(RouteModel(**route))
        return routes

    async def get_by_id(self, id: str) -> Optional[RouteModel]:
        if not ObjectId.is_valid(id):
            return None
        route = await self.collection.find_one({"_id": ObjectId(id)})
        if route:
            return RouteModel(**route)
        return None

    async def get_by_sport_id(self, sport_id: str) -> List[RouteModel]:
        if not ObjectId.is_valid(sport_id):
            return []

        routes_cursor = self.collection.find({"sport_id": sport_id})
        routes = []
        async for route in routes_cursor:
            routes.append(RouteModel(**route))
        return routes

    async def insert_one(self, route_data: dict):
        result = await self.collection.insert_one(route_data)
        return result.inserted_id

    async def update_by_id(self, route_id: str, update_data: dict) -> bool:
        if not ObjectId.is_valid(route_id):
            return False

        result = await self.collection.update_one(
            {"_id": ObjectId(route_id)},
            {"$set": update_data}
        )
        return result.modified_count > 0