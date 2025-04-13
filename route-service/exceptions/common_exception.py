from fastapi import HTTPException, status


class DuplicateKeyException(HTTPException):
    def __init__(self, resource_name: str, field_name: str, field_value: str):
        detail = f"{resource_name} with {field_name} '{field_value}' already exists."
        super().__init__(status_code=status.HTTP_409_CONFLICT, detail=detail)


class MissingRequiredFieldException(HTTPException):
    def __init__(self, field_name: str, message: str = None):
        detail = (
            f"Field '{field_name}' error: {message}"
            if message else
            f"Field '{field_name}' is required and cannot be null."
        )
        super().__init__(status_code=status.HTTP_400_BAD_REQUEST, detail=detail)


class ResourceAlreadyExistsException(HTTPException):
    def __init__(self, resource_name: str, field_name: str, field_value: str):
        detail = f"{resource_name} with the {field_name} {field_value} already exists."
        super().__init__(status_code=status.HTTP_400_BAD_REQUEST, detail=detail)


class ResourceNotFoundException(HTTPException):
    def __init__(self, resource_name: str, field_name: str, field_value: str):
        detail = f"{resource_name} not found with {field_name}: '{field_value}'"
        super().__init__(status_code=status.HTTP_404_NOT_FOUND, detail=detail)


class StrideException(HTTPException):
    def __init__(self, status_code: int = status.HTTP_500_INTERNAL_SERVER_ERROR, message: str = "Internal server error"):
        super().__init__(status_code=status_code, detail=message)
