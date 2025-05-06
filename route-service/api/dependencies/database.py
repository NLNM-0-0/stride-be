from typing import AsyncGenerator
from sqlalchemy.ext.asyncio import AsyncSession
from repositories.database import async_session
from loguru import logger


async def get_postgres_session() -> AsyncGenerator[AsyncSession, None]:
    try:
        logger.debug("Getting PostgreSQL session")
        async with async_session() as session:
            yield session
    except Exception as e:
        logger.error(f"PostgreSQL session error: {e}")
        raise
