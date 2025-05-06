import requests

from dto.file.response.file_link_response import FileLinkResponse


class BridgeClient:
    def __init__(self, base_url: str):
        self.base_url = base_url.rstrip("/")

    def upload_raw_file(
        self,
        data: bytes,
        file_name: str,
        content_type: str = "image/png"
    ) -> FileLinkResponse:
        url = f"{self.base_url}/files/raw"
        headers = {
            "Content-Type": "application/octet-stream",
            "Accept": "application/json"
        }
        params = {
            "fileName": file_name,
            "contentType": content_type
        }
        response = requests.post(url, headers=headers, params=params, data=data)
        response.raise_for_status()
        return FileLinkResponse(**response.json())
