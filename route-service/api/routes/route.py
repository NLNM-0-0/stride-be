from fastapi import APIRouter, Depends, status
from typing import List

from api.dependencies.repository import get_repository
from clients.mapbox_client import MapboxClient
from clients.overpass_client import OverpassClient
from configuration.manager import settings
from dto.route.request.create_route_request import CreateRouteRequest
from dto.route.request.get_recommend_route_request import GetRecommendRouteRequest
from dto.route.request.update_route_request import UpdateRouteRequest
from dto.route.response.create_route_response import CreateRouteResponse
from dto.route.response.route_response import RouteResponse
from dto.simple_response import SimpleResponse
from repositories.crud.route_repository import RouteRepository
from services.mapbox_service import MapboxService
from services.overpass_service import OverpassService
from services.route_service import RouteService

route_router = APIRouter(prefix="/stride-routes", tags=["Routes"])

def get_mapbox_service() -> MapboxService:
    return MapboxService(mapbox_client=MapboxClient(base_url=settings.MAPBOX_URL))


def get_overpass_service() -> OverpassService:
    return OverpassService(overpass_client=OverpassClient(base_url=settings.OVERPASS_URL))

def get_route_service(
        route_repository: RouteRepository = Depends(get_repository(RouteRepository)),
        mapbox_service: MapboxService = Depends(get_mapbox_service),
        overpass_service: OverpassService = Depends(get_overpass_service),
) -> RouteService:
    return RouteService(
        route_repository=route_repository,
        mapbox_service=mapbox_service,
        overpass_service=overpass_service
    )


@route_router.get(
    path="",
    response_model=List[RouteResponse],
    status_code=status.HTTP_200_OK,
)
async def get_all_routes(service: RouteService = Depends(get_route_service)):
    return await service.get_all_routes()


@route_router.post(
    path="/recommend",
    response_model=List[RouteResponse]
)
async def get_recommended_routes(
        request: GetRecommendRouteRequest,
        service: RouteService = Depends(get_route_service)
):
    return await service.get_recommended_routes(request)


@route_router.post(
    path="",
    response_model=CreateRouteResponse,
    status_code=status.HTTP_201_CREATED
)
async def create_route(
        request: CreateRouteRequest,
        service: RouteService = Depends(get_route_service)
):
    return await service.create_route(request)


@route_router.put(
    path="/{route_id}",
    response_model=SimpleResponse
)
async def update_route(
        route_id: str,
        request: UpdateRouteRequest,
        service: RouteService = Depends(get_route_service)
):
    await service.update_route(route_id, request)
    return SimpleResponse()
