from models.route_model import RouteModel


class RouteSpecs:
    @staticmethod
    def has_user(user_id: str) -> callable:
        return lambda query: query.filter(RouteModel.user_id == user_id)

    @staticmethod
    def has_sport(sport_id: str) -> callable:
        return lambda query: query.filter(RouteModel.sport_id == sport_id)