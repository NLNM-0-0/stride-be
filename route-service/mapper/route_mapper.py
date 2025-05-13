from geoalchemy2 import WKBElement
from pydantic import BaseModel
from shapely import wkb
from shapely.geometry.multilinestring import MultiLineString

from constants.geometry_encode_type import GeometryEncodeType
from dto.route.response.location_response import LocationResponse
from dto.route.response.route_response import RouteResponse
from models.route_model import RouteModel
from utils.geometry_helper import GeometryHelper


class RouteMapper(BaseModel):
    @staticmethod
    def map_to_route_response(route: RouteModel) -> RouteResponse:
        raw_images = route.images or {}
        images = [img for img_list in raw_images.values() for img in img_list]

        location_data = route.location

        return RouteResponse(
            id=str(route.id),
            user_id=route.user_id,
            sport_id=route.sport_id,
            name=route.name,
            avg_time=route.total_time / route.heat,
            avg_distance=route.total_distance / route.heat,
            location=LocationResponse(
                ward=location_data.get("ward"),
                district=location_data.get("district"),
                city=location_data.get("city"),
            ),
            map_image=route.map_image,
            images=images,
            geometry=RouteMapper._encode_geometry_to_polyline(route.geometry),
            districts=route.districts,
            heat=route.heat,
        )

    @staticmethod
    def _encode_geometry_to_polyline(geom_wkb: WKBElement, precision: int = 5) -> str | None:
        if geom_wkb is None:
            return None

        shapely_geom = wkb.loads(bytes(geom_wkb.data))

        if shapely_geom.geom_type == "LineString":
            coords = [[lon, lat] for lon, lat in shapely_geom.coords]
            return GeometryHelper.encode_geometry(coords, GeometryEncodeType.GEOJSON)

        elif shapely_geom.geom_type == "MultiLineString" and isinstance(shapely_geom, MultiLineString):
            coords = []
            for line in shapely_geom.geoms:
                coords.extend([[lon, lat] for lon, lat in line.coords])
            return GeometryHelper.encode_geometry(coords, GeometryEncodeType.GEOJSON)

        return None