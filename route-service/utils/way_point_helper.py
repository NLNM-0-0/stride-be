from constants.map_type import SportMapType
from dto.supbase.response.way_point import WayPoint


class WayPointHelper:
    @staticmethod
    def get_rdp_epsilon(map_type: SportMapType) -> float:
        if map_type == SportMapType.WALKING:
            return 0.00005
        return 0.001

    @staticmethod
    def get_simplification_step(map_type: SportMapType) -> int:
        if map_type == SportMapType.WALKING:
            return 5
        return 10

    @staticmethod
    def filter_points(points: list[WayPoint]) -> list[WayPoint]:
        filtered = WayPointHelper._remove_none(points)
        filtered = WayPointHelper._filter_by_local_repetition(filtered)
        filtered = WayPointHelper._keep_run_boundaries(filtered)
        return filtered

    @staticmethod
    def _remove_none(points: list[WayPoint]) -> list[WayPoint]:
        """Remove None entries from the list."""
        return [p for p in points if p is not None]

    @staticmethod
    def _filter_by_local_repetition(points: list[WayPoint], window_size: int = 2, min_count: int = 3) -> list[WayPoint]:
        """
        Keep points that appear at least `min_count` times within a sliding window around each point.
        """
        n = len(points)
        if n < window_size * 2 + 1:
            return points

        result = []

        for i in range(window_size):
            result.append(points[i])


        for i in range(window_size, n - window_size):
            window = points[i - window_size: i + window_size + 1]
            name = points[i].name
            count = sum(1 for p in window if p.name == name)
            if count >= min_count:
                result.append(points[i])

        for i in range(n - window_size, n):
            result.append(points[i])

        return result

    @staticmethod
    def _keep_run_boundaries(points: list[WayPoint]) -> list[WayPoint]:
        """
        Keep only the first and last point of each run of consecutive points with the same name.
        """
        if not points:
            return []

        result = []
        i = 0
        while i < len(points):
            start = i
            current_name = points[i].name
            while i + 1 < len(points) and points[i + 1].name == current_name:
                i += 1
            end = i
            result.append(points[start])
            if end != start:
                result.append(points[end])
            i += 1

        return result
