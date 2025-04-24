from fastapi import APIRouter, status

ping_router = APIRouter(prefix="", tags=["Ping"])

@ping_router.get(
    "/ping",
    response_model=str,
    status_code=status.HTTP_200_OK,
)
async def ping():
    return "pong"