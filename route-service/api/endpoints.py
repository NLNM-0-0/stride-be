import fastapi

from api.routes.ping import ping_router
from api.routes.route import route_router

router = fastapi.APIRouter()

router.include_router(router=route_router)
router.include_router(router=ping_router)