import requests

class SupabaseClient:
    def __init__(self, base_url: str, token: str):
        self.base_url = base_url.rstrip("/")
        self.token = token

    def _post(self, path: str, data: dict, timeout: int = 30) -> dict:
        url = f"{self.base_url}{path}"
        headers = {
            "Authorization": f"Bearer {self.token}",
            "Content-Type": "application/json",
            "Accept": "application/json"
        }
        response = requests.post(url, headers=headers, json=data, timeout=timeout)
        response.raise_for_status()
        return response.json()

    def get_location_by_geometry(self, data: dict) -> dict:
        return self._post("/functions/v1/get_location_by_geometry", data)

    def find_districts_near_point(self, data: dict) -> dict:
        return self._post("/functions/v1/find_districts_near_point", data)

    def find_nearest_way_points(self, data: dict) -> dict:
        return self._post("/functions/v1/find_nearest_way_points", data)