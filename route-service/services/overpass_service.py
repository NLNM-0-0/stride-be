from typing import List

from clients.overpass_client import OverpassClient
from dto.overpass.response.overpass_geocoding_reverse_response import OverpassGeocodingReverseResponse
from exceptions.common_exception import StrideException


class OverpassService:
    overpass_client: OverpassClient

    def __init__(self, overpass_client: OverpassClient):
        self.overpass_client = overpass_client

    def fetch_nearby_localities(
            self,
            latitude: float,
            longitude: float,
            around: int
    ) -> List[str]:
        query_string = f"""
        [out:json];
        (
          relation(around:{around},{latitude},{longitude})["admin_level"="6"];
        );
        out tags;
        """

        response = self.overpass_client.query(data=query_string)

        if response.status_code != 200:
            raise StrideException(
                message=f"Error fetching localities around lat: {latitude}, lon: {longitude}. "
                        f"Status code: {response.status_code}")

        locations = response.json()
        localities = [element['tags'].get('name', '') for element in locations['elements']]
        return localities

    def reverse_geocoding(
            self,
            latitude: float,
            longitude: float
    ) -> OverpassGeocodingReverseResponse:
        query_string = f"""
        [out:json];
        is_in({latitude},{longitude})->.a;
        (
          area.a["admin_level"~"4|6|8"];
        );
        out tags;
        """

        response = self.overpass_client.query(data=query_string)

        if response.status_code != 200:
            raise StrideException(
                message=f"Error fetching localities around lat: {latitude}, lon: {longitude}. "
                        f"Status code: {response.status_code}")

        locations = response.json()

        place = "None"
        locality ="None"
        neighborhood = "None"

        for element in locations.get("elements", {}):
            tags = element.get("tags", {})
            admin_level = tags.get("admin_level")

            if admin_level == "4":
                place = tags.get("name")
            elif admin_level == "6":
                locality = tags.get("name")
            elif admin_level == "8":
                neighborhood = tags.get("name")

        return OverpassGeocodingReverseResponse(
            neighborhood=neighborhood,
            place=place,
            locality=locality,
        )

