from enum import Enum

class SportMapType(Enum):
    DRIVING = ("driving", 10, 1000)
    WALKING = ("walking", 1, 100)
    CYCLING = ("cycling", 10, 1000)

    def __init__(self, lowercase: str, record_meters: int, recommended_distance: int):
        self.lowercase = lowercase
        self.record_meters = record_meters
        self.recommended_distance = recommended_distance
