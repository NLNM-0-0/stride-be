from datetime import datetime, timezone

from fastapi import FastAPI, Request, status
from fastapi.responses import JSONResponse

from dto.error_response import ErrorResponse
from exceptions.common_exception import (
    DuplicateKeyException,
    MissingRequiredFieldException,
    ResourceAlreadyExistsException,
    ResourceNotFoundException,
    StrideException,
)


def register_exception_handlers(app: FastAPI):

    def build_error_response(exc: Exception, status_code: int, request: Request):
        error = ErrorResponse(
            timestamp=datetime.now(timezone.utc),
            status=status_code,
            message=str(exc),
            detail=str(request.url),
        )
        return JSONResponse(
            status_code=status_code,
            content=error.model_dump(),
        )

    @app.exception_handler(DuplicateKeyException)
    async def handle_duplicate_key_exception(request: Request, exc: DuplicateKeyException):
        return build_error_response(exc, exc.status_code, request)

    @app.exception_handler(MissingRequiredFieldException)
    async def handle_missing_field_exception(request: Request, exc: MissingRequiredFieldException):
        return build_error_response(exc, exc.status_code, request)

    @app.exception_handler(ResourceAlreadyExistsException)
    async def handle_resource_already_exist_exception(request: Request, exc: ResourceAlreadyExistsException):
        return build_error_response(exc, exc.status_code, request)

    @app.exception_handler(ResourceNotFoundException)
    async def handle_resource_not_found_exception(request: Request, exc: ResourceNotFoundException):
        return build_error_response(exc, exc.status_code, request)

    @app.exception_handler(StrideException)
    async def handle_stride_exception(request: Request, exc: StrideException):
        return build_error_response(exc, exc.status_code, request)

    @app.exception_handler(Exception)
    async def handle_generic_exception(request: Request, exc: Exception):
        return build_error_response(exc, status.HTTP_500_INTERNAL_SERVER_ERROR, request)