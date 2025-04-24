import uplink


@uplink.timeout(10)
class OverpassClient(uplink.Consumer):
    @uplink.post("/api/interpreter")
    def query(
            self,
            data: uplink.Body(str)
    ):
        pass