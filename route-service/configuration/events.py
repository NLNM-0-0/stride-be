import typing

import fastapi
import loguru

from repositories.events import dispose_mongo_connection, initialize_mongo_connection


def execute_backend_server_event_handler(backend_app: fastapi.FastAPI) -> typing.Any:
    async def launch_backend_server_events() -> None:
        await initialize_mongo_connection(backend_app=backend_app)

    return launch_backend_server_events


def terminate_backend_server_event_handler(backend_app: fastapi.FastAPI) -> typing.Any:
    @loguru.logger.catch
    async def stop_backend_server_events() -> None:
        await dispose_mongo_connection(backend_app=backend_app)

    return stop_backend_server_events
