import uplink


@uplink.timeout(10)
@uplink.headers({
    "Accept": "application/json"
})
class OverpassClient(uplink.Consumer):
    @uplink.post("/api/interpreter")
    def query(
            self,
            data: uplink.Body(str)
    ):
        pass