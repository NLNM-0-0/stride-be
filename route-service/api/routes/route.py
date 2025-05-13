from fastapi import APIRouter, Depends, status, Request

from api.dependencies.mapbox_service import get_mapbox_service
from api.dependencies.repository import get_repository
from api.dependencies.supabase_service import get_supabase_service
from constants.custom_headers import CustomHeaders
from dto.page.app_page_request import AppPageRequest
from dto.route.request.create_route_request import CreateRouteRequest
from dto.route.request.get_recommend_route_request import GetRecommendRouteRequest
from dto.route.request.route_filter import RouteFilter
from dto.route.request.save_route_request import SaveRouteRequest
from dto.route.request.update_route_request import UpdateRouteRequest
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
)
async def get_all_routes(service: RouteService = Depends(get_route_service)):
    return await service.get_all_routes()


@route_router.post(
    path="/recommend",
)
async def get_recommended_routes(
        body: GetRecommendRouteRequest,
        service: RouteService = Depends(get_route_service)
):
    return await service.get_recommended_routes(body)


@route_router.get(
    path="/profile",
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


@route_router.post(
    path="",
    status_code=status.HTTP_201_CREATED,
)
async def create_route(
        body: CreateRouteRequest,
        service: RouteService = Depends(get_route_service)
):
    return await service.create_route(
        request=body,
    )


@route_router.post(
    path="/{route_id}/save",
    status_code=status.HTTP_201_CREATED,
)
async def save_route(
        request: Request,
        route_id: str,
        body: SaveRouteRequest,
        service: RouteService = Depends(get_route_service)
):
    user_id = AuthHelper.get_auth_header(request, CustomHeaders.X_AUTH_USER_ID)

    return await service.save_route(
        user_id=user_id,
        route_id=route_id,
        request=body,
    )


@route_router.put(
    path="/{route_id}",
)
async def update_route(
        request: Request,
        route_id: str,
        body: UpdateRouteRequest,
        service: RouteService = Depends(get_route_service)
):
    user_id = AuthHelper.get_auth_header(request, CustomHeaders.X_AUTH_USER_ID)

    await service.update_route(user_id, route_id, body)
    return SimpleResponse()


@route_router.delete(
    path="/{route_id}",
)
async def delete_route(
        request: Request,
        route_id: str,
        service: RouteService = Depends(get_route_service)
):
    user_id = AuthHelper.get_auth_header(request, CustomHeaders.X_AUTH_USER_ID)

    await service.delete_route(user_id, route_id)
    return SimpleResponse()
