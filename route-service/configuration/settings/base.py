from pydantic_settings import BaseSettings
import pathlib
import logging

from configuration.settings.environment import Environment

ROOT_DIR = pathlib.Path(__file__).parent.parent.parent.resolve()

class BackendBaseSettings(BaseSettings):
    TITLE: str = "route-service"
    TIMEZONE: str = "UTC"
    DEBUG: bool = False

    ENVIRONMENT: Environment = Environment.DEVELOPMENT

    SERVER_HOST: str
    SERVER_PORT: int
    SERVER_WORKERS: int
    API_PREFIX: str

    DB_MONGO_HOST: str
    DB_MONGO_PORT: str
    DB_MONGO_USERNAME: str
    DB_MONGO_PASSWORD: str
    DB_MONGO_NAME: str

    IS_ALLOWED_CREDENTIALS: bool
    ALLOWED_ORIGINS: list[str] = []
    ALLOWED_METHODS: list[str] = ["*"]
    ALLOWED_HEADERS: list[str] = ["*"]

    LOGGING_LEVEL: int = logging.INFO
    LOGGERS: tuple[str, str] = ("uvicorn.asgi", "uvicorn.access")

    model_config = {
        "env_file": f"{ROOT_DIR}/.env",
        "case_sensitive": True,
    }

    @property
    def set_backend_app_attributes(self) -> dict[str, str | bool | None]:
        return {
            "title": self.TITLE,
            "api_prefix": self.API_PREFIX,
            "debug": self.DEBUG,
        }
