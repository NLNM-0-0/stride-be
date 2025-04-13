from configuration.settings.base import BackendBaseSettings
from configuration.settings.environment import Environment

class BackendDevSettings(BackendBaseSettings):
    DESCRIPTION: str | None = "Development Environment."
    DEBUG: bool = True
    ENVIRONMENT: Environment = Environment.DEVELOPMENT
