import uplink


@uplink.timeout(10)
@uplink.headers({"Content-Type": "application/json"})
class MapboxClient(uplink.Consumer):

    @uplink.get("/users/{user_id}")
    def get_user(self, user_id: str) -> uplink.Response:
        """Lấy thông tin người dùng theo ID"""