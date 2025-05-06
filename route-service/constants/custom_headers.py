from enum import Enum


class CustomHeaders(Enum):
    X_AUTH_USER_ID = "X-Auth-User-Id"
    X_AUTH_USERNAME = "X-Auth-Username"
    X_AUTH_EMAIL = "X-Auth-Email"
    X_AUTH_PROVIDER = "X-Auth-Provider"
    X_AUTH_USER_AUTHORITIES = "X-Auth-User-Authorities"
    X_REQUEST_ID = "X-Request-ID"