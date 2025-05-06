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

    DB_SCHEMA: str
    DB_URL: str

    IS_ALLOWED_CREDENTIALS: bool
    ALLOWED_ORIGINS: list[str] = []
    ALLOWED_METHODS: list[str] = ["*"]
    ALLOWED_HEADERS: list[str] = ["*"]

    MAPBOX_URL: str
    MAPBOX_TOKEN: str
    MAPBOX_STYLE: str
    MAPBOX_WIDTH: int
    MAPBOX_HEIGHT: int
    MAPBOX_PADDING: int
    MAPBOX_CONTENT_TYPE: str
    MAPBOX_STROKE_WIDTH: int
    MAPBOX_STROKE_COLOR: str
    MAPBOX_STROKE_FILL: str

    BRIDGE_SERVICE_URL: str

    SUPABASE_TOKEN: str
    SUPABASE_URL: str

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
