import fastapi
import uvicorn
from fastapi.middleware.cors import CORSMiddleware

from api.endpoints import router
from configuration.events import execute_backend_server_event_handler, terminate_backend_server_event_handler
from configuration.manager import settings


def initialize_backend_application() -> fastapi.FastAPI:
    app = fastapi.FastAPI(**settings.set_backend_app_attributes)  # type: ignore

    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.ALLOWED_ORIGINS,
        allow_credentials=settings.IS_ALLOWED_CREDENTIALS,
        allow_methods=settings.ALLOWED_METHODS,
        allow_headers=settings.ALLOWED_HEADERS,
    )

    app.add_event_handler(
        "startup",
        execute_backend_server_event_handler(backend_app=app),
    )
    app.add_event_handler(
        "shutdown",
        terminate_backend_server_event_handler(backend_app=app),
    )

    app.include_router(router=router, prefix=settings.API_PREFIX)

    return app


route_service: fastapi.FastAPI = initialize_backend_application()

if __name__ == "__main__":
    uvicorn.run(
        app="main:route_service",
        host=settings.SERVER_HOST,
        port=settings.SERVER_PORT,
        reload=settings.DEBUG,
        workers=settings.SERVER_WORKERS,
        log_level=settings.LOGGING_LEVEL,
    )