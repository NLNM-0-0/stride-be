from pydantic import BaseModel

from dto.route.response.route_response import RouteResponse
from dto.route.response.route_short_response import RouteShortResponse
from models.route_model import RouteModel


class RouteMapper(BaseModel):
    @staticmethod
    def map_to_route_response(route: RouteModel) -> RouteResponse:
        raw_images = route.images or {}
        images = [img for img_list in raw_images.values() for img in img_list]

        return RouteResponse(
            id=route.id,
            user_id=route.user_id,
            sport_id=route.sport_id,
            name=route.name,
            avg_time=route.total_time / route.heat,
            avg_distance=route.total_distance / route.heat,
            location=route.location,
            map_image=route.map_image,
            images=images,
            geometry=route.geometry,
            district=route.district,
            heat=route.heat,
        )

    @staticmethod
    def map_to_route_short_response(route: RouteModel) -> RouteShortResponse:
        return RouteShortResponse(
            id=route.id,
            user_id=route.user_id,
            sport_id=route.sport_id,
            name=route.name,
            avg_time=route.total_time / route.heat,
            avg_distance=route.total_distance / route.heat,
            location=route.location,
            map_image=route.map_image,
            heat=route.heat,
        )