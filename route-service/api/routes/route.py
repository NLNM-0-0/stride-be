from fastapi import APIRouter, Depends, status, Request
from typing import List

from api.dependencies.mapbox_service import get_mapbox_service
from api.dependencies.repository import get_repository
from api.dependencies.supabase_service import get_supabase_service
from constants.custom_headers import CustomHeaders
from dto.app_page_request import AppPageRequest
from dto.route.request.create_route_request import CreateRouteRequest
from dto.route.request.get_recommend_route_request import GetRecommendRouteRequest
from dto.route.request.route_filter import RouteFilter
from dto.route.request.update_route_request import UpdateRouteRequest
from dto.route.response.create_route_response import CreateRouteResponse
from dto.route.response.route_response import RouteResponse
from dto.simple_response import SimpleResponse
from repositories.crud.route_repository import RouteRepository
from services.mapbox_service import MapboxService
from services.route_service import RouteService
from services.supabase_service import SupabaseService
from utils.auth_helper import AuthHelper

route_router = APIRouter(prefix="/stride-routes", tags=["Routes"])

def get_route_service(
        route_repository: RouteRepository = Depends(get_repository(RouteRepository)),
        mapbox_service: MapboxService = Depends(get_mapbox_service),
        supabase_service: SupabaseService = Depends(get_supabase_service),
) -> RouteService:
    return RouteService(
        route_repository=route_repository,
        mapbox_service=mapbox_service,
        supabase_service=supabase_service
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
        body: GetRecommendRouteRequest,
        service: RouteService = Depends(get_route_service)
):
    return await service.get_recommended_routes(body)


@route_router.get(
    path="/profile",
    response_model=List[RouteResponse],
    status_code=status.HTTP_200_OK,
)
async def get_user_route(
        request: Request,
        route_filter: RouteFilter = Depends(),
        page: AppPageRequest = Depends(),
        service: RouteService = Depends(get_route_service)
):
    user_id = AuthHelper.get_auth_header(request, CustomHeaders.X_AUTH_USER_ID)
    route_filter.user_id = user_id

    return await service.get_routes(
        route_filter=route_filter,
        page=page,
    )

@route_router.get(
    path="/{route_id}",
    response_model=RouteResponse
)
async def get_route(
        route_id: str,
        service: RouteService = Depends(get_route_service)
):
    response = await service.get_route(route_id)
    return response

@route_router.post(
    path="",
    response_model=CreateRouteResponse,
    status_code=status.HTTP_201_CREATED
)
async def create_route(
        request: Request,
        body: CreateRouteRequest,
        service: RouteService = Depends(get_route_service)
):
    user_id = AuthHelper.get_auth_header(request, CustomHeaders.X_AUTH_USER_ID)

    return await service.create_route(
        user_id=user_id,
        request=body,
    )

@route_router.put(
    path="/{route_id}",
    response_model=SimpleResponse
)
async def update_route(
        route_id: str,
        body: UpdateRouteRequest,
        service: RouteService = Depends(get_route_service)
):
    await service.update_route(route_id, body)
    return SimpleResponse()
