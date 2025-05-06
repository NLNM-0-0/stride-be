from clients.bridge_client import BridgeClient
from clients.mapbox_client import MapboxClient
from configuration.manager import settings
from services.mapbox_service import MapboxService


def get_mapbox_service() -> MapboxService:
    return MapboxService(
        mapbox_client=MapboxClient(
            base_url=settings.MAPBOX_URL,
            access_token=settings.MAPBOX_TOKEN
        ),
        bridge_client=BridgeClient(
            base_url=settings.BRIDGE_SERVICE_URL,
        )
    )