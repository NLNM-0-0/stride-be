import uplink


@uplink.timeout(60)
@uplink.headers({
    "Accept": "application/json"
})
class MapboxClient(uplink.Consumer):
    """Client để gọi Mapbox Directions API"""

    @uplink.get("/directions/v5/mapbox/{map_type}/{coordinates}")
    def get_directions(
            self,
            map_type: uplink.Path(),
            coordinates: uplink.Path(),
            access_token: uplink.Query(),
            alternatives: uplink.Query() = "false",
            geometries: uplink.Query() = "geojson",
            overview: uplink.Query() = "simplified",
            steps: uplink.Query() = "false"
    ):
        pass

    @uplink.get("/search/searchbox/v1/reverse")
    def reverse_geocoding(
            self,
            longitude: uplink.Query(),
            latitude: uplink.Query(),
            limit: uplink.Query(),
            access_token: uplink.Query(),
    ):
        pass