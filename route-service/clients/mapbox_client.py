import uplink

@uplink.timeout(10)
@uplink.headers({
    "Accept": "application/json"
})
class MapboxClient(uplink.Consumer):
    """Client để gọi Mapbox Directions API"""

    @uplink.get("/directions/v5/mapbox/{map_type}/{coordinates}")
    def get_directions(
        self,
        map_type: uplink.Path(str),
        coordinates: uplink.Path(str),
        access_token: uplink.Query(str),
        alternatives: uplink.Query(str) = "false",
        geometries: uplink.Query(str) = "geojson",
        overview: uplink.Query(str) = "simplified",
        steps: uplink.Query(str) = "false"
    ):
        """Lấy chỉ đường từ Mapbox"""