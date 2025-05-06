from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession

from configuration.manager import settings

engine = create_async_engine(
    settings.DB_URL,
    echo=True,
    connect_args={
        "server_settings": {"search_path": settings.DB_SCHEMA}
    }
)
async_session = async_sessionmaker(engine, expire_on_commit=False, class_=AsyncSession)