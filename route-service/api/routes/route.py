import fastapi
from fastapi import APIRouter, Depends, status
from typing import List

from api.dependencies.repository import get_repository
from dto.request.create_route_request import CreateRouteRequest
from dto.request.get_recommend_route_request import GetRecommendRouteRequest
from dto.request.update_route_request import UpdateRouteRequest
from dto.response.route_response import RouteResponse
from dto.response.simple_response import SimpleResponse
from repositories.crud.route_repository import RouteRepository
from services.route_service import RouteService

route_router = APIRouter(prefix="/routes", tags=["Routes"])

def get_route_service(
    route_repository: RouteRepository = Depends(get_repository(RouteRepository)),
) -> RouteService:
    return RouteService(route_repository=route_repository)

@route_router.get(
    "/",
    response_model=List[RouteResponse],
    status_code=status.HTTP_200_OK,
)
async def get_all_routes(service: RouteService = Depends(get_route_service)):
    return await service.get_all_routes()


@route_router.get("/recommend", response_model=List[RouteResponse])
async def get_recommended_routes(
        request: GetRecommendRouteRequest,
        service: RouteService = Depends(get_route_service)
):
    return await service.get_recommended_routes(request)


@route_router.post("/", response_model=SimpleResponse, status_code=status.HTTP_201_CREATED)
async def create_route(
        request: CreateRouteRequest,
        service: RouteService = Depends(get_route_service)
):
    await service.create_route(request)
    return SimpleResponse()


@route_router.put("/{route_id}", response_model=SimpleResponse)
async def update_route(
        route_id: str,
        request: UpdateRouteRequest,
        service: RouteService = Depends(get_route_service)
):
    await service.update_route(route_id, request)
    return SimpleResponse()
