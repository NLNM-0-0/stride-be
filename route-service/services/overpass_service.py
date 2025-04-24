from typing import List

from clients.overpass_client import OverpassClient
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
          relation(around:{around},{latitude},{longitude})["admin_level"="8"];
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

