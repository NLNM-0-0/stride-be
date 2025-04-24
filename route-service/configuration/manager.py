from functools import lru_cache

from configuration.settings.base import BackendBaseSettings


@lru_cache()
def get_settings() -> BackendBaseSettings:
    return BackendBaseSettings()  # Tự động load từ `.env`

settings = get_settings()