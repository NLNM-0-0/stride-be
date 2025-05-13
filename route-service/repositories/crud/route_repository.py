from uuid import UUID

from geoalchemy2 import WKBElement
from sqlalchemy import select, Row, RowMapping, text, func, and_
from typing import Optional, Any, Sequence

from dto.route.request.route_filter import RouteFilter
from models.route_model import RouteModel
from repositories.base import BaseSQLRepository
from repositories.specs.route_specs import RouteSpecs


class RouteRepository(BaseSQLRepository):
    async def get_all(self) -> Sequence[RouteModel]:
        result = await self.session.execute(select(RouteModel))
        return result.scalars().all()

    async def get_by_filters(
        self,
        route_filter: RouteFilter,
    ) -> Sequence[Row[Any] | RowMapping | Any]:
        query = select(RouteModel)

        query = RouteSpecs.has_user(route_filter.user_id)(query)
        if route_filter.sport_id:
            query = RouteSpecs.has_sport(route_filter.sport_id)(query)

        query = query.order_by(RouteModel.heat.desc())

        result = await self.session.execute(query)
        return result.scalars().all()

    async def get_by_filters_and_paging(
            self,
            route_filter: RouteFilter,
            page: int,
            limit: int,
    ) -> Sequence[RouteModel]:
        query = select(RouteModel)

        if route_filter.user_id:
            query = RouteSpecs.has_user(route_filter.user_id)(query)
        if route_filter.sport_id:
            query = RouteSpecs.has_sport(route_filter.sport_id)(query)

        query = query.order_by(RouteModel.created_at.desc())

        query = query.offset((page - 1) * limit).limit(limit)

        result = await self.session.execute(query)
        return result.scalars().all()

    async def get_by_id(self, route_id: UUID) -> Optional[RouteModel]:
        result = await self.session.get(RouteModel, route_id)
        return result

    async def insert_one(self, data: RouteModel) -> RouteModel:
        self.session.add(data)
        await self.session.commit()
        await self.session.refresh(data)
        return data

    async def update_route(self, route: RouteModel, update_data: dict) -> bool:
        for key, value in update_data.items():
            setattr(route, key, value)
        await self.session.commit()
        return True

    async def delete(self, route: RouteModel):
        if not route:
            return False
        await self.session.delete(route)
        await self.session.commit()

    async def find_most_similar_route(self, geometry: WKBElement) -> RouteModel:
        hausdorff_distance = func.ST_HausdorffDistance(RouteModel.geometry, geometry)

        query = (
            select(RouteModel)
            .where(
                and_(
                    RouteModel.user_id == None,
                    func.ST_DWithin(RouteModel.geometry, geometry, 1),
                    hausdorff_distance <= 0.0001
                )
            )
            .order_by(hausdorff_distance)
            .limit(1)
        )

        result = await self.session.execute(query)
        return result.scalars().first()