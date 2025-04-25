from pydantic_settings import BaseSettings, SettingsConfigDict
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

    DB_MONGO_NAME: str
    DB_MONGO_URL: str

    IS_ALLOWED_CREDENTIALS: bool
    ALLOWED_ORIGINS: list[str] = []
    ALLOWED_METHODS: list[str] = ["*"]
    ALLOWED_HEADERS: list[str] = ["*"]

    MAPBOX_URL: str
    MAPBOX_TOKEN: str

    OVERPASS_URL: str

    LOGGING_LEVEL: int = logging.INFO
    LOGGERS: tuple[str, str] = ("uvicorn.asgi", "uvicorn.access")

    model_config = SettingsConfigDict(
        env_file=None,  # Will be set dynamically
        env_file_encoding='utf-8',
        case_sensitive=True,
        extra='ignore'
    )

    @property
    def set_backend_app_attributes(self) -> dict[str, str | bool | None]:
        return {
            "title": self.TITLE,
            "api_prefix": self.API_PREFIX,
            "debug": self.DEBUG,
        }
