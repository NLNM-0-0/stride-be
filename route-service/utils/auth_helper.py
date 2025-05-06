from fastapi import Request

from constants.custom_headers import CustomHeaders
from exceptions.common_exception import StrideException


class AuthHelper:
    @staticmethod
    def get_auth_header(request: Request, header_type: CustomHeaders.X_AUTH_USER_ID) -> str:
        user_id = request.headers.get(header_type.value)
        if not user_id:
            raise StrideException(
                message=f"Missing {header_type} header"
            )
        return user_id