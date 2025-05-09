from sqlalchemy.ext.asyncio import AsyncSession


class BaseSQLRepository:
    def __init__(self, session: AsyncSession):
        self.session = session