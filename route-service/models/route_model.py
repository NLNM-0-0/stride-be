import uuid

from sqlalchemy import Column, String, Float, Integer, JSON, DateTime, func
from sqlalchemy.dialects.postgresql import UUID

from configuration.manager import settings
from models.base_model import Base


class RouteModel(Base):
    __tablename__ = "routes"
    __table_args__ = {"schema": settings.DB_SCHEMA}

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    user_id = Column(String, nullable=True)
    sport_id = Column(String, nullable=False)
    name = Column(String, nullable=False)
    total_time = Column(Float, default=0)
    total_distance = Column(Float, default=0)
    location = Column(JSON, nullable=False)
    map_image = Column(String, nullable=False)
    images = Column(JSON, nullable=False, default=dict)
    geometry = Column(String, nullable=False)
    districts = Column(JSON, nullable=False)
    heat = Column(Integer, default=0)

    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)
