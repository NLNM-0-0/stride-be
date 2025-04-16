import fastapi

from api.routes.route import route_router

router = fastapi.APIRouter()

router.include_router(router=route_router)