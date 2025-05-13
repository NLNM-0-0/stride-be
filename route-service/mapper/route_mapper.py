from pydantic import BaseModel
from shapely.geometry.base import BaseGeometry
from shapely.geometry.multilinestring import MultiLineString

from dto.route.response.location_response import LocationResponse
from dto.route.response.route_response import RouteResponse
from models.route_model import RouteModel


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
    def _encode_geometry_to_polyline(geom: BaseGeometry, precision: int = 5) -> str | None:
        import polyline

        if geom is None:
            return None

        if geom.geom_type == "LineString":
            coords = [(lat, lon) for lon, lat in geom.coords]
            return polyline.encode(coords, precision=precision)

        elif geom.geom_type == "MultiLineString" and isinstance(geom, MultiLineString):
            coords = []
            for line in geom.geoms:
                coords.extend([(lat, lon) for lon, lat in line.coords])
            return polyline.encode(coords, precision=precision)

        return None