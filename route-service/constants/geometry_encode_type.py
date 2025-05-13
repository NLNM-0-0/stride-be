from enum import Enum


class GeometryEncodeType(Enum):
    GEOJSON = "geojson"

    WKB = "wkb"

    URL = "url"