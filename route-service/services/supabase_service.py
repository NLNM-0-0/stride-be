from clients.supabase_client import SupabaseClient
from dto.supbase.request.find_districts_near_point_request import FindDistrictNearPointRequest
from dto.supbase.request.find_nearest_way_points_request import FindNearestWayPointsRequest
from dto.supbase.request.get_location_by_geometry_request import GetGeometryByLocationRequest
from dto.supbase.response.district_response import DistrictResponse
from dto.supbase.response.find_districts_near_point_response import FindDistrictNearPointResponse
from dto.supbase.response.find_nearest_way_points_response import FindNearestWayPointsResponse
from dto.supbase.response.get_location_by_geometry_response import GetLocationByGeometryResponse
from dto.supbase.response.way_point import WayPoint
from exceptions.common_exception import StrideException


class SupabaseService:
    supabase_client: SupabaseClient

    def __init__(self, supabase_client: SupabaseClient):
        self.supabase_client = supabase_client

    def get_location_by_geometry(self, data: GetGeometryByLocationRequest) -> GetLocationByGeometryResponse:
        try:
            response = self.supabase_client.get_location_by_geometry(
                data=data.model_dump()
            )
            return GetLocationByGeometryResponse(**response)
        except Exception as e:
            raise StrideException(
                message=f"Error get location for geometry {data.geometry}. Detail: {e}"
            ) from e

    def find_districts_near_point(self, data: FindDistrictNearPointRequest) -> FindDistrictNearPointResponse:
        try:
            response = self.supabase_client.find_districts_near_point(
                data=data.model_dump()
            )
            districts = [
                DistrictResponse(**item)
                for item in response.get("data", [])
                if item is not None
            ]
            return FindDistrictNearPointResponse(data=districts)
        except Exception as e:
            raise StrideException(
                message=f"Error find district near point for lat {data.lat} lon {data.lon} around {data.around}. Detail: {e}"
            ) from e

    def find_nearest_way_points(self, data: FindNearestWayPointsRequest) -> FindNearestWayPointsResponse:
        try:
            response = self.supabase_client.find_nearest_way_points(
                data=data.model_dump()
            )
            waypoints = [WayPoint(**item) for item in response if item is not None]
            return FindNearestWayPointsResponse(data=waypoints)
        except Exception as e:
            raise StrideException(
                message=f"Error finding nearest way points. Detail: {e}"
            ) from e


