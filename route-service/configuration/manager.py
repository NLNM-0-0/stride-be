import os
from functools import lru_cache

from configuration.settings.base import BackendBaseSettings, ROOT_DIR


@lru_cache()
def get_settings() -> BackendBaseSettings:
    env_name = os.getenv("APP_ENV", "dev")
    env_file_path = ROOT_DIR / f"deploy/.env.{env_name}"
    return BackendBaseSettings(_env_file=env_file_path)

settings = get_settings()