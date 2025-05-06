import requests


class MapboxClient:
    def __init__(self, base_url: str, access_token: str):
        self.base_url = base_url.rstrip("/")
        self.access_token = access_token

    def get_directions(
        self,
        map_type: str,
        coordinates: str,
        alternatives: str = "false",
        geometries: str = "geojson",
        overview: str = "simplified",
        steps: str = "false"
    ) -> dict:
        url = f"{self.base_url}/directions/v5/mapbox/{map_type}/{coordinates}"
        params = {
            "access_token": self.access_token,
            "alternatives": alternatives,
            "geometries": geometries,
            "overview": overview,
            "steps": steps
        }
        response = requests.get(url, params=params)
        response.raise_for_status()
        return response.json()

    def get_static_map_image(
        self,
        map_style: str,
        stroke_width: str,
        stroke_color: str,
        stroke_fill: str,
        path: str,
        width: int,
        height: int,
        padding: int = 0
    ) -> bytes:
        path_part = f"path-{stroke_width}{stroke_color}{stroke_fill}({path})"
        url = f"{self.base_url}/styles/v1/{map_style}/static/{path_part}/auto/{width}x{height}"
        params = {
            "access_token": self.access_token,
            "padding": padding
        }
        response = requests.get(url, params=params)
        response.raise_for_status()
        return response.content
